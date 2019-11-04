package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

public class UserProfileUpdateRequestValidatorImplTest {

    @Test
    public void testValidateRequestIfBothStatusAndRoleArePresent() {

        Set<RoleName> rolesData = new HashSet<>();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-organisation-manager");
        rolesData.add(roleName1);
        rolesData.add(roleName2);

        Set<RoleName> rolesToDeleteData = new HashSet<>();
        RoleName roleToDeleteName = new RoleName("pui-finance-manager");
        rolesToDeleteData.add(roleToDeleteName);

        UserProfileUpdatedData userProfileUpdatedData =
                new UserProfileUpdatedData("test@test.com", "fname", "lname", IdamStatus.ACTIVE.name(), rolesData, rolesToDeleteData);


        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);

        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isNull();
        assertThat(actualModifyProfileData.getRolesAdd()).containsOnly(roleName1, roleName2);
        assertThat(actualModifyProfileData.getRolesDelete()).containsOnly(roleToDeleteName);

    }

    @Test
    public void testValidateRequestForStatus() {

        UserProfileUpdatedData userProfileUpdatedData =
                new UserProfileUpdatedData("test@test.com", "fname", "lname", IdamStatus.ACTIVE.name(), null, null);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isEqualTo("ACTIVE");
        assertThat(actualModifyProfileData.getRolesAdd()).isNull();
        assertThat(actualModifyProfileData.getRolesDelete()).isNull();

    }

    @Test
    public void testValidateRequestForRoles() {

        Set<RoleName> rolesData = new HashSet<>();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-organisation-manager");
        rolesData.add(roleName1);
        rolesData.add(roleName2);

        Set<RoleName> rolesToDeleteData = new HashSet<>();
        RoleName roleToDeleteName = new RoleName("pui-finance-manager");
        rolesToDeleteData.add(roleToDeleteName);


        UserProfileUpdatedData userProfileUpdatedData =
                new UserProfileUpdatedData("test@test.com", "fname", "lname", null, rolesData, rolesToDeleteData);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isNull();
        assertThat(actualModifyProfileData.getRolesAdd()).containsOnly(roleName1, roleName2);
        assertThat(actualModifyProfileData.getRolesDelete()).containsOnly(roleToDeleteName);
    }

    @Test(expected = InvalidRequest.class)
    public void testThrowErrorIfValidateRequestIsEmpty() {

        UserProfileUpdatedData userProfileUpdatedData =
                new UserProfileUpdatedData("test@test.com", "fname", "lname", null, null, null);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);

    }


}