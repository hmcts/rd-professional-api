package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.AccessType;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserProfileUpdateRequestValidatorImplTest {

    private final String email = "test@test.com";
    private final String firstName = "fname";
    private final String lastName = "lname";
    private final String puiOrganisationManager = "pui-organisation-manager";
    private final String puiCaseManager = "pui-case-manager";
    private final String puiFinanceManager = "pui-finance-manager";

    private final Set<RoleName> rolesData = new HashSet<>();
    private final Set<RoleName> rolesToDeleteData = new HashSet<>();
    private final Set<AccessType> accessTypes = new HashSet<>();
    private RoleName roleName1;
    private RoleName roleName2;
    private RoleName roleToDeleteName;
    private AccessType accessType1;

    @BeforeEach
    void setUp() {
        roleName1 = new RoleName(puiCaseManager);
        roleName2 = new RoleName(puiOrganisationManager);
        roleToDeleteName = new RoleName(puiFinanceManager);

        rolesData.add(roleName1);
        rolesData.add(roleName2);
        rolesToDeleteData.add(roleToDeleteName);

        accessType1 = new AccessType();
        accessTypes.add(accessType1);
    }

    @Test
    void test_ValidateRequestIfBothStatusAndRoleArePresent() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName,
                IdamStatus.ACTIVE.name(), rolesData, rolesToDeleteData, null);

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
                IdamStatus.ACTIVE.name(), null, null, null);

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
                null, rolesData, rolesToDeleteData, null);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isNull();
        assertThat(actualModifyProfileData.getRolesAdd()).containsOnly(roleName1, roleName2);
        assertThat(actualModifyProfileData.getRolesDelete()).containsOnly(roleToDeleteName);
    }

    @Test
    void test_ValidateRequestForAccessTypes() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName,
                IdamStatus.ACTIVE.name(), null, null, accessTypes);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        UserProfileUpdatedData actualModifyProfileData = sut.validateRequest(userProfileUpdatedData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());
        assertThat(actualModifyProfileData.getRolesAdd()).isNull();
        assertThat(actualModifyProfileData.getRolesDelete()).isNull();
        assertThat(actualModifyProfileData.getAccessTypes().size()).isEqualTo(1);
    }

    @Test
    void test_ThrowErrorIfValidateRequestIsEmpty() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData(email, firstName, lastName,
                null, null, null, null);

        UserProfileUpdateRequestValidator sut = new UserProfileUpdateRequestValidatorImpl();
        assertThrows(InvalidRequest.class, () ->
                sut.validateRequest(userProfileUpdatedData));
    }
}
