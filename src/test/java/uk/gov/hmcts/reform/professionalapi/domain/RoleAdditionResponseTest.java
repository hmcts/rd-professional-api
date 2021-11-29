package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleAdditionResponseTest {

    private String idamStatusCode = "Code";
    private String idamMessage = "Message";

    @Test
    void test_addRoleResponse() {
        RoleAdditionResponse addRoleResponse = new RoleAdditionResponse(idamStatusCode, idamMessage);

        assertThat(addRoleResponse.getIdamStatusCode()).isEqualTo(idamStatusCode);
        assertThat(addRoleResponse.getIdamMessage()).isEqualTo(idamMessage);
    }
}