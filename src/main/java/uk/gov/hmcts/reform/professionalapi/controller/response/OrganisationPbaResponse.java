package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

public class OrganisationPbaResponse {


    @JsonProperty
    private PaymentAccountResponse organisations;

    public OrganisationPbaResponse(Organisation organisation) {

        getOrganisationPbaResponse(organisation);
    }

    private void getOrganisationPbaResponse(Organisation organisation) {

        this.organisations = new PaymentAccountResponse(organisation);

    }

}
