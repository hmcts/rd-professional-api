package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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
        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();
        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();
        orgAttributeRequest.setKey("TestKey");
        orgAttributeRequest.setValue("TestValue");
        orgAttributeRequests.add(orgAttributeRequest);
        organisationOtherOrgsCreationRequest.setOrgAttributes(orgAttributeRequests);

        assertThat(organisationOtherOrgsCreationRequest.getName()).isEqualTo("test");
        assertThat(organisationOtherOrgsCreationRequest.getStatus()).isEqualTo("ACTIVE");
        assertThat(organisationOtherOrgsCreationRequest.getStatusMessage()).isEqualTo("In review");
        assertThat(organisationOtherOrgsCreationRequest.getSraId()).isEqualTo("sra-id");
        assertThat(organisationOtherOrgsCreationRequest.getSraRegulated()).isEqualTo("false");
        assertThat(organisationOtherOrgsCreationRequest.getOrgTypeKey()).isEqualTo("Doctor");
        assertThat(organisationOtherOrgsCreationRequest.getOrgAttributes()).isNotNull();

    }
}
