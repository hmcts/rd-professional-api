package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

public class ModifyUserProfileDataTest {

    @Test
    public void should_add_roles_add_when_modified() {

        ModifyUserProfileData modifyUserProfileData1 = new ModifyUserProfileData(){};

        String idamId = UUID.randomUUID().toString();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> rolesAdd = new HashSet<RoleName>();
        rolesAdd.add(roleName1);
        rolesAdd.add(roleName2);
        Set<RoleName> rolesDelete = new HashSet<RoleName>();
        rolesDelete.add(roleName1);
        rolesDelete.add(roleName2);
        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData("a@hmcts.net","fname","lname", "ACTIVE",rolesAdd,rolesDelete);
        modifyUserProfileData.setRolesAdd(rolesAdd);
        modifyUserProfileData.setRolesDelete(rolesDelete);

        assertThat(modifyUserProfileData.getRolesAdd().size()).isEqualTo(2);
        assertThat(modifyUserProfileData.getRolesDelete().size()).isEqualTo(2);
        assertThat(modifyUserProfileData.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(modifyUserProfileData.getFirstName()).isEqualTo("fname");
        assertThat(modifyUserProfileData.getLastName()).isEqualTo("lname");
        assertThat(modifyUserProfileData.getIdamStatus()).isEqualTo("ACTIVE");

    }
}