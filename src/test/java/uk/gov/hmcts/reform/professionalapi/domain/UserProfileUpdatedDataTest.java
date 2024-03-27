package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        UserAccessType userAccessType1 = new UserAccessType("jurisdictionId", "organisationProfileId", "accessTypeId",
                false);
        Set<UserAccessType> userAccessTypes = new HashSet<>();
        userAccessTypes.add(userAccessType1);

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(UUID.randomUUID().toString(),"test@test.com", "fname", "lname",
                "ACTIVE", rolesAdd, rolesDelete, userAccessTypes);
        userProfileUpdatedData.setRolesAdd(rolesAdd);
        userProfileUpdatedData.setRolesDelete(rolesDelete);

        assertThat(userProfileUpdatedData.getRolesAdd()).hasSize(2);
        assertThat(userProfileUpdatedData.getRolesAdd()).containsAll(rolesAdd);
        assertThat(userProfileUpdatedData.getRolesDelete()).hasSize(2);
        assertThat(userProfileUpdatedData.getRolesDelete()).containsAll(rolesDelete);
        assertThat(userProfileUpdatedData.getEmail()).isEqualTo("test@test.com");
        assertThat(userProfileUpdatedData.getFirstName()).isEqualTo("fname");
        assertThat(userProfileUpdatedData.getLastName()).isEqualTo("lname");
        assertThat(userProfileUpdatedData.getIdamStatus()).isEqualTo("ACTIVE");
    }
}
