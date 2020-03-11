package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RoleDeletionResponseTest {

    private String roleName = "Role";
    private String idamStatusCode = "Code";
    private String idamMessage = "Message";

    @Test
    public void deleteRoleResponseTest() {
        RoleDeletionResponse deleteRoleResponse = new RoleDeletionResponse(roleName, idamStatusCode, idamMessage);

        assertThat(deleteRoleResponse.getRoleName()).isEqualTo(roleName);
        assertThat(deleteRoleResponse.getIdamStatusCode()).isEqualTo(idamStatusCode);
        assertThat(deleteRoleResponse.getIdamMessage()).isEqualTo(idamMessage);
    }
}