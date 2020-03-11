package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

public class UserProfileUpdateRequestValidatorImplTest {

    private String email = "test@test.com";
    private String firstName = "fname";
    private String lastName = "lname";
    private String puiOrganisationManager = "pui-organisation-manager";
    private String puiCaseManager = "pui-case-manager";
    private String puiFinanceManager = "pui-finance-manager";

    private Set<RoleName> rolesData = new HashSet<>();
    private Set<RoleName> rolesToDeleteData = new HashSet<>();
    private RoleName roleName1;
    private RoleName roleName2;
    private RoleName roleToDeleteName;

    @Before
    public void setUp() {
        roleName1 = new RoleName(puiCaseManager);
        roleName2 = new RoleName(puiOrganisationManager);
        roleToDeleteName = new RoleName(puiFinanceManager);

        rolesData.add(roleName1);
        rolesData.add(roleName2);
        rolesToDeleteData.add(roleToDeleteName);
    }

    @Test
    public void testValidateRequestIfBothStatusAndRoleArePresent() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName, IdamStatus.ACTIVE.name(), rolesData, rolesToDeleteData);

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
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName, IdamStatus.ACTIVE.name(), null, null);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());
        assertThat(actualModifyProfileData.getRolesAdd()).isNull();
        assertThat(actualModifyProfileData.getRolesDelete()).isNull();
    }

    @Test
    public void testValidateRequestForRoles() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName, null, rolesData, rolesToDeleteData);

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
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName, null, null, null);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        sut.validateRequest(userProfileUpdatedData);
    }
}