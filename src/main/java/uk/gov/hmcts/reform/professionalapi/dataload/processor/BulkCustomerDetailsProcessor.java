package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.dataload.binder.BulkCustomerDetails;
import uk.gov.hmcts.reform.professionalapi.dataload.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.FAILURE;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.PrdLoadUtils.setFileStatus;

@Component
@Slf4j
public class BulkCustomerDetailsProcessor extends JsrValidationBaseProcessor<BulkCustomerDetails> {

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    @Qualifier("JsrValidatorInitializerDataload")
    JsrValidatorInitializer<BulkCustomerDetails> bulkCustomerDetailsJsrValidatorInitializer;

    public static final String ORGANISATION_ID = "organisationId";
    public static final String ERROR_MSG = "organisationId Not present in Organisation";

    @Override
    @SuppressWarnings("unchecked")
    public void process(Exchange exchange) {

        List<BulkCustomerDetails> bulkCustomerDetails;

        bulkCustomerDetails = (exchange.getIn().getBody() instanceof List)
            ? (List<BulkCustomerDetails>) exchange.getIn().getBody()
            : singletonList((BulkCustomerDetails) exchange.getIn().getBody());
        log.info(" {} bulkCustomerDetails Records count before Validation {}::",
            bulkCustomerDetails.size()
        );
        List<BulkCustomerDetails> finalBulkCustomerDetails = getValidCategories(bulkCustomerDetails);

        log.info(" {} Categories Records count after Validation {}::",
            finalBulkCustomerDetails.size()
        );

        if (bulkCustomerDetails.size() != finalBulkCustomerDetails.size()) {
            String auditStatus = PARTIAL_SUCCESS;
            if (finalBulkCustomerDetails.isEmpty()) {
                auditStatus = FAILURE;
            }
            setFileStatus(exchange, applicationContext, auditStatus);
        }

        exchange.getMessage().setBody(finalBulkCustomerDetails);
        List<BulkCustomerDetails> invalidBulkCustomers =
            getInvalidCategories(bulkCustomerDetails, finalBulkCustomerDetails);
        List<Pair<String, Long>> invalidBulkCustomerIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(invalidBulkCustomers)) {
            invalidBulkCustomers.forEach(bulkCustomer -> invalidBulkCustomerIds.add(Pair.of(
                bulkCustomer.getOrganisationId(),
                bulkCustomer.getRowId()
            )));

            bulkCustomerDetailsJsrValidatorInitializer.auditJsrExceptions(
                invalidBulkCustomerIds,
                ORGANISATION_ID,
                ERROR_MSG,
                exchange
            );
        }
    }

    private List<BulkCustomerDetails> getInvalidCategories(List<BulkCustomerDetails> bulkCustomerDetails,
                                                           List<BulkCustomerDetails> finalBulkCustomerDetails) {

        List<BulkCustomerDetails> invalidBulkCustomerDetails = new ArrayList<>(bulkCustomerDetails);

        invalidBulkCustomerDetails.removeAll(finalBulkCustomerDetails);

        return invalidBulkCustomerDetails;
    }

    private List<BulkCustomerDetails> getValidCategories(List<BulkCustomerDetails> bulkCustomerDetails) {
        return bulkCustomerDetails.stream()
            .filter(bulkCustomerDetail -> organisationRepository
                .findById(UUID.fromString(bulkCustomerDetail.getOrganisationId())).isPresent()).toList();

    }
}
