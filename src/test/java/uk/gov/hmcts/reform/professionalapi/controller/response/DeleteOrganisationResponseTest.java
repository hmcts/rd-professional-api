package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class DeleteOrganisationResponseTest {

    @Test
    public void test_DeleteOrganisationResponseTest() {

        final int statusCode = 204;
        final String message = "successfully deleted";
        final String errorDescription = "failed to delete";

        final DeleteOrganisationResponse deleteOrganisationResponse = new DeleteOrganisationResponse();
        deleteOrganisationResponse.setStatusCode(statusCode);
        deleteOrganisationResponse.setMessage(message);
        deleteOrganisationResponse.setErrorDescription(errorDescription);
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(statusCode);
        assertThat(deleteOrganisationResponse.getMessage()).isEqualTo(message);
        assertThat(deleteOrganisationResponse.getErrorDescription()).isEqualTo(errorDescription);
    }
}
