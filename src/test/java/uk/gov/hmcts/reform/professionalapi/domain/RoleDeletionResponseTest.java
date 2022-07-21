package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RoleDeletionResponseTest {

    private final String roleName = "Role";
    private final String idamStatusCode = "Code";
    private final String idamMessage = "Message";

    @Test
    void test_deleteRoleResponse() {
        RoleDeletionResponse deleteRoleResponse = new RoleDeletionResponse(roleName, idamStatusCode, idamMessage);

        assertThat(deleteRoleResponse.getRoleName()).isEqualTo(roleName);
        assertThat(deleteRoleResponse.getIdamStatusCode()).isEqualTo(idamStatusCode);
        assertThat(deleteRoleResponse.getIdamMessage()).isEqualTo(idamMessage);
    }
}