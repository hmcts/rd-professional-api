package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RoleAdditionResponseTest {

    private String idamStatusCode = "Code";
    private String idamMessage = "Message";

    @Test
    public void test_addRoleResponse() {
        RoleAdditionResponse addRoleResponse = new RoleAdditionResponse(idamStatusCode, idamMessage);

        assertThat(addRoleResponse.getIdamStatusCode()).isEqualTo(idamStatusCode);
        assertThat(addRoleResponse.getIdamMessage()).isEqualTo(idamMessage);
    }
}