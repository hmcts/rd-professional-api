package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RoleNameTest {

    private static final  String PUI_USER_MANAGER = "pui-user-manager";
    private static final String PUI_CASE_MANAGER = "pui-case-manager";

    @Test
    public void test_should_hold_values_after_creation() {
        RoleName roleName = new RoleName();
        roleName.setName(PUI_CASE_MANAGER);
        RoleName roleName1 = new RoleName(PUI_USER_MANAGER);

        assertThat(roleName.getName()).isEqualTo(PUI_CASE_MANAGER);
        assertThat(roleName1.getName()).isEqualTo(PUI_USER_MANAGER);
    }
}