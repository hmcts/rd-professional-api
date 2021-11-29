package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

@ExtendWith(MockitoExtension.class)
class UserProfileUpdateRequestValidatorImplTest {

    private final String email = "test@test.com";
    private final String firstName = "fname";
    private final String lastName = "lname";

    private final Set<RoleName> rolesData = new HashSet<>();
    private final Set<RoleName> rolesToDeleteData = new HashSet<>();
    private RoleName roleName1;
    private RoleName roleName2;
    private RoleName roleToDeleteName;

    @BeforeEach
    void setUp() {
        String puiCaseManager = "pui-case-manager";
        roleName1 = new RoleName(puiCaseManager);
        String puiOrganisationManager = "pui-organisation-manager";
        roleName2 = new RoleName(puiOrganisationManager);
        String puiFinanceManager = "pui-finance-manager";
        roleToDeleteName = new RoleName(puiFinanceManager);
        rolesData.add(roleName1);
        rolesData.add(roleName2);
        rolesToDeleteData.add(roleToDeleteName);
    }

    @Test
    void test_ValidateRequestIfBothStatusAndRoleArePresent() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName,
                IdamStatus.ACTIVE.name(), rolesData, rolesToDeleteData);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);

        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isNull();
        assertThat(actualModifyProfileData.getRolesAdd()).containsOnly(roleName1, roleName2);
        assertThat(actualModifyProfileData.getRolesDelete()).containsOnly(roleToDeleteName);
    }

    @Test
    void test_ValidateRequestForStatus() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName,
                IdamStatus.ACTIVE.name(), null, null);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());
        assertThat(actualModifyProfileData.getRolesAdd()).isNull();
        assertThat(actualModifyProfileData.getRolesDelete()).isNull();
    }

    @Test
    void test_ValidateRequestForRoles() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName,
                null, rolesData, rolesToDeleteData);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isNull();
        assertThat(actualModifyProfileData.getRolesAdd()).containsOnly(roleName1, roleName2);
        assertThat(actualModifyProfileData.getRolesDelete()).containsOnly(roleToDeleteName);
    }

    @Test
    void test_ThrowErrorIfValidateRequestIsEmpty() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName,
                null, null, null);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        assertThrows(InvalidRequest.class, () ->
                sut.validateRequest(userProfileUpdatedData));
    }
}