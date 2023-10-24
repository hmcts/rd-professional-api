package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.domain.BulkCustomerDetails;

public class BulkCustomerOrganisationsDetailResponse {

    @JsonProperty
    @Getter
    private final String organisationId;


    @JsonProperty
    @Getter
    private final String organisationName;

    @JsonProperty
    @Getter
    private final String paymentAccount;

    public BulkCustomerOrganisationsDetailResponse(BulkCustomerDetails bulkCustomerDetails) {
        organisationId = bulkCustomerDetails.getOrganisation().getOrganisationIdentifier();
        organisationName = bulkCustomerDetails.getOrganisation().getName();
        paymentAccount = bulkCustomerDetails.getPbaNumber();
    }
}
