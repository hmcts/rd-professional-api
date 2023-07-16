package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationOtherOrgsCreationRequestTest {

    @Test
    void test_OrganisationCreationRequest() {

        OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest =
                new OrganisationOtherOrgsCreationRequest("test", "PENDING", null, "sra-id", "false",
                        "number02", "company-url", null, null,
                        null,"Doctor",null);

        organisationOtherOrgsCreationRequest.setStatus("ACTIVE");
        organisationOtherOrgsCreationRequest.setStatusMessage("In review");

        assertThat(organisationOtherOrgsCreationRequest.getName()).isEqualTo("test");
        assertThat(organisationOtherOrgsCreationRequest.getStatus()).isEqualTo("ACTIVE");
        assertThat(organisationOtherOrgsCreationRequest.getStatusMessage()).isEqualTo("In review");
        assertThat(organisationOtherOrgsCreationRequest.getSraId()).isEqualTo("sra-id");
        assertThat(organisationOtherOrgsCreationRequest.getSraRegulated()).isEqualTo("false");
        assertThat(organisationOtherOrgsCreationRequest.getOrgTypeKey()).isEqualTo("Doctor");

    }


}
