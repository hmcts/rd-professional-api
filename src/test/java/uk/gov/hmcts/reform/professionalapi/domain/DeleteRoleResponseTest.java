package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteRoleResponseTest {

    @Test
    public void deleteRoleResponseTest() {
        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse("Role","Code","Message");

        assertThat(deleteRoleResponse.getRoleName()).isEqualTo("Role");
        assertThat(deleteRoleResponse.getIdamStatusCode()).isEqualTo("Code");
        assertThat(deleteRoleResponse.getIdamMessage()).isEqualTo("Message");
    }
}