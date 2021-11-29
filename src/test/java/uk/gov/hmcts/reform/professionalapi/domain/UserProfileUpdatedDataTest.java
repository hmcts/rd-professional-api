package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileUpdatedDataTest {

    @Test
    void test_should_add_roles_add_when_modified() {
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> rolesAdd = new HashSet<>();
        rolesAdd.add(roleName1);
        rolesAdd.add(roleName2);

        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(roleName1);
        rolesDelete.add(roleName2);

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData("a@hmcts.net",
                "fname", "lname", "ACTIVE", rolesAdd, rolesDelete);
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