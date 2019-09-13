package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

public class ModifyUserProfileDataTest {

    @Test
    public void should_add_roles_add_when_modified() {

        String idamId = UUID.randomUUID().toString();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<RoleName>();
        roles.add(roleName1);
        roles.add(roleName2);
        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData("a@hmcts.net","fname","lname", "ACTIVE",roles);
        modifyUserProfileData.setRolesAdd(roles);

        assertThat(modifyUserProfileData.getRolesAdd().size()).isEqualTo(2);
        assertThat(modifyUserProfileData.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(modifyUserProfileData.getFirstName()).isEqualTo("fname");
        assertThat(modifyUserProfileData.getLastName()).isEqualTo("lname");
        assertThat(modifyUserProfileData.getIdamStatus()).isEqualTo("ACTIVE");

    }
}