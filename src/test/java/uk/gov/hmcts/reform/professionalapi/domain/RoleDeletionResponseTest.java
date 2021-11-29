package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleDeletionResponseTest {

    private String roleName = "Role";
    private String idamStatusCode = "Code";
    private String idamMessage = "Message";

    @Test
    void test_deleteRoleResponse() {
        RoleDeletionResponse deleteRoleResponse = new RoleDeletionResponse(roleName, idamStatusCode, idamMessage);

        assertThat(deleteRoleResponse.getRoleName()).isEqualTo(roleName);
        assertThat(deleteRoleResponse.getIdamStatusCode()).isEqualTo(idamStatusCode);
        assertThat(deleteRoleResponse.getIdamMessage()).isEqualTo(idamMessage);
    }
}