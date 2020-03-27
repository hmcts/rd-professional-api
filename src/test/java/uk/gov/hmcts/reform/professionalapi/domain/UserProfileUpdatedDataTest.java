package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_CASE_MANAGER;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class UserProfileUpdatedDataTest {

    @Test
    public void should_add_roles_add_when_modified() {
        RoleName roleName1 = new RoleName(PUI_CASE_MANAGER);
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> rolesAdd = new HashSet<>();
        rolesAdd.add(roleName1);
        rolesAdd.add(roleName2);

        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(roleName1);
        rolesDelete.add(roleName2);

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData("a@hmcts.net", "fname", "lname", "ACTIVE", rolesAdd, rolesDelete);
        userProfileUpdatedData.setRolesAdd(rolesAdd);
        userProfileUpdatedData.setRolesDelete(rolesDelete);

        assertThat(userProfileUpdatedData.getRolesAdd().size()).isEqualTo(2);
        assertThat(userProfileUpdatedData.getRolesAdd().containsAll(rolesAdd)).isTrue();
        assertThat(userProfileUpdatedData.getRolesDelete().size()).isEqualTo(2);
        assertThat(userProfileUpdatedData.getRolesDelete().containsAll(rolesDelete)).isTrue();
        assertThat(userProfileUpdatedData.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(userProfileUpdatedData.getFirstName()).isEqualTo("fname");
        assertThat(userProfileUpdatedData.getLastName()).isEqualTo("lname");
        assertThat(userProfileUpdatedData.getIdamStatus()).isEqualTo("ACTIVE");
    }
}