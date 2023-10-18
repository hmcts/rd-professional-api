package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.dataload.config.AzureBlobConfig;
import uk.gov.hmcts.reform.professionalapi.dataload.exception.RouteFailedException;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;
import uk.gov.hmcts.reform.professionalapi.dataload.service.AuditServiceImpl;
import uk.gov.hmcts.reform.professionalapi.dataload.util.BlobStatus;

import java.util.Date;
import java.util.function.BiPredicate;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.time.DateUtils.isSameDay;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.BlobStatus.NEW;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.BlobStatus.NOT_EXISTS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.BlobStatus.STALE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.BLOBPATH;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.FILE_NOT_EXISTS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_FILE_STALE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_NOT_BLANK;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_START_ROUTE_JRD;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.MILLIS_IN_A_DAY;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.NOT_STALE_FILE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.SCHEDULER_START_TIME;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.STALE_FILE_ERROR;

/**
 * This FileReadProcessor checks if file has following status
 * Stale: old File then do nothing and log error in Audit table
 * Not-exist in blob then do nothing and log error in Audit table
 * New File for today's date the consumes the file and stores in Message body.
 *
 * @since 2020-10-27
 */
@Slf4j
@Component
public class FileReadProcessor implements Processor {

    @Value("${file-read-time-out}")
    private int fileReadTimeOut;

    @Value("${azure.storage.account-key}")
    private String azureKey;

    @Value("${idam.s2s-auth.totp_secret}")
    private String s2sSecret;

    @Value("${logging-component-name:data_ingestion}")
    private String logComponentName;

    @Autowired
    private AzureBlobConfig azureBlobConfig;

    @Autowired
    private AuditServiceImpl auditService;

    private Date fileTimeStamp;

    @Autowired
    @Qualifier("credscloudStorageAccount")
    private CloudStorageAccount cloudStorageAccount;

    /**
     * Consumes files only if it is not Stale and stores it in message body.
     *
     * @param exchange Exchange
     */
    @Override
    public void process(Exchange exchange) {
        log.info("{}:: FileReadProcessor starts::", logComponentName);
        final String blobFilePath = (String) exchange.getProperty(BLOBPATH);
        log.info("{}:: BLOBPATH::", blobFilePath);
        log.info("{}:: azureKey starts::",azureKey);
        log.info("{}:: s2sSecret starts::",s2sSecret);
        final CamelContext context = exchange.getContext();
        final ConsumerTemplate consumer = context.createConsumerTemplate();
        RouteProperties routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
        String fileName = routeProperties.getFileName();
        String schedulerTime = context.getGlobalOption(SCHEDULER_START_TIME);

        //Check Stale OR Not existing file and exit run with proper error message
        if (getFileStatusInBlobContainer(routeProperties, schedulerTime).equals(STALE)) {
            exchange.getMessage().setHeader(IS_FILE_STALE, true);
            throw new RouteFailedException(String.format(STALE_FILE_ERROR,
                fileName, DateFormatUtils.format(fileTimeStamp, "yyyy-MM-dd HH:mm:SS")));
        } else if (getFileStatusInBlobContainer(routeProperties, schedulerTime).equals(NOT_EXISTS)) {
            throw new RouteFailedException(String.format(FILE_NOT_EXISTS,
                routeProperties.getFileName()));
        }

        exchange.getMessage().setHeader(IS_FILE_STALE, false);
        context.getGlobalOptions().put(fileName, NOT_STALE_FILE);
        exchange.getMessage().setBody(consumer.receiveBody(blobFilePath, fileReadTimeOut));
    }

    /**
     * Connects to Blob storage and checks if file timestamp is stale.
     *
     * @param routeProperties for getting fileName and start route
     * @param schedulerTime time when the scheduler starts
     * @return is File TimeStamp Stale
     */
    private BlobStatus getFileStatusInBlobContainer(RouteProperties routeProperties, String schedulerTime) {
        try {

            CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
            CloudBlobContainer container =
                blobClient.getContainerReference(azureBlobConfig.getContainerName());

            CloudBlockBlob cloudBlockBlob = container.getBlockBlobReference(routeProperties.getFileName());

            //if scheduler time not set via camel context then set as current date else camel context pass current
            // data time
            if (isEmpty(schedulerTime)) {
                schedulerTime = String.valueOf(new Date(System.currentTimeMillis()).getTime());
            }

            if (cloudBlockBlob.exists()) {
                cloudBlockBlob.downloadAttributes();
                fileTimeStamp = cloudBlockBlob.getProperties().getLastModified();

                BlobStatus blobStatus;
                Date today = new Date(Long.parseLong(schedulerTime));

                if (IS_NOT_BLANK.and(IS_START_ROUTE_JRD).test(routeProperties.getStartRoute())) {
                    blobStatus = isSameDay.or(isPrevDay).test(fileTimeStamp, today)
                            ? NEW
                            : STALE;
                } else {
                    blobStatus = isSameDay.test(fileTimeStamp, today) ? NEW : STALE;
                }

                return blobStatus;
            }

            return NOT_EXISTS;
        } catch (Exception exp) {
            log.error("{}:: Failed to get file timestamp :: ", logComponentName, exp);
            throw new RouteFailedException("Failed to get file timestamp ::");
        }
    }

    public static final BiPredicate<Date, Date> isSameDay = DateUtils::isSameDay;
    public static final BiPredicate<Date, Date> isPrevDay = (fileTS, schedulerDate) ->
        isSameDay(fileTS, new Date(schedulerDate.getTime() - MILLIS_IN_A_DAY));
}
