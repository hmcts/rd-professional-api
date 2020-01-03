package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

public class UserProfileUpdatedDataTest {

    @Test
    public void should_add_roles_add_when_modified() {

        UserProfileUpdatedData userProfileUpdatedData1 = new UserProfileUpdatedData(){};

        String idamId = UUID.randomUUID().toString();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> rolesAdd = new HashSet<RoleName>();
        rolesAdd.add(roleName1);
        rolesAdd.add(roleName2);
        Set<RoleName> rolesDelete = new HashSet<RoleName>();
        rolesDelete.add(roleName1);
        rolesDelete.add(roleName2);
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData("a@hmcts.net","fname","lname", "ACTIVE",rolesAdd,rolesDelete);
        userProfileUpdatedData.setRolesAdd(rolesAdd);
        userProfileUpdatedData.setRolesDelete(rolesDelete);

        assertThat(userProfileUpdatedData.getRolesAdd().size()).isEqualTo(2);
        assertThat(userProfileUpdatedData.getRolesDelete().size()).isEqualTo(2);
        assertThat(userProfileUpdatedData.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(userProfileUpdatedData.getFirstName()).isEqualTo("fname");
        assertThat(userProfileUpdatedData.getLastName()).isEqualTo("lname");
        assertThat(userProfileUpdatedData.getIdamStatus()).isEqualTo("ACTIVE");

    }
}