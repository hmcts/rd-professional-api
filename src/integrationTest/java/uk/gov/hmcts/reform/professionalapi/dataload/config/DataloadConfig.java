package uk.gov.hmcts.reform.professionalapi.dataload.config;

import com.microsoft.azure.storage.CloudStorageAccount;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.ExceptionProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.FileReadProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.processor.HeaderValidationProcessor;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;
import uk.gov.hmcts.reform.professionalapi.dataload.service.AuditServiceImpl;

import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.IS_FILE_STALE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.NOT_STALE_FILE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;

@Configuration
public class DataloadConfig {

    @MockBean
    @Qualifier("credscloudStorageAccount")
    CloudStorageAccount cloudStorageAccount;

    @MockBean
    BlobStorageCredentials blobStorageCredentials;

    @Bean
    FileReadProcessor fileReadProcessor() {
        return new FileReadProcessor() {
            @Override
            public void process(Exchange exchange) {
                String blobFilePath = (String)exchange.getProperty("blob-path");
                CamelContext context = exchange.getContext();
                ConsumerTemplate consumer = context.createConsumerTemplate();
                exchange.getMessage().setHeader(IS_FILE_STALE, false);
                RouteProperties routeProperties = (RouteProperties) exchange.getIn().getHeader(ROUTE_DETAILS);
                String fileName = routeProperties.getFileName();
                context.getGlobalOptions().put(fileName, NOT_STALE_FILE);
                exchange.getMessage().setBody(consumer.receiveBody(blobFilePath));
            }
        };
    }

    @Bean
    public ExceptionProcessor exceptionProcessor() {
        return new ExceptionProcessor();
    }

    @Bean
    public AuditServiceImpl auditServiceImpl() {
        return new AuditServiceImpl();
    }

    @Bean
    public HeaderValidationProcessor headerValidationProcessor() {
        return new HeaderValidationProcessor();
    }
}
