package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteOrganisationResponseTest {

    @Test
    void test_DeleteOrganisationResponseTest() {

        final int statusCode = 204;
        final String message = "successfully deleted";
        final DeleteOrganisationResponse deleteOrganisationResponse = new DeleteOrganisationResponse();
        deleteOrganisationResponse.setStatusCode(statusCode);
        deleteOrganisationResponse.setMessage(message);
        assertThat(deleteOrganisationResponse.getStatusCode()).isEqualTo(statusCode);
        assertThat(deleteOrganisationResponse.getMessage()).isEqualTo(message);
    }
}
