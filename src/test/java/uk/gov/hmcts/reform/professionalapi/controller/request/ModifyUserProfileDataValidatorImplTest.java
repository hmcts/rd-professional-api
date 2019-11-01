package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;


public class ModifyUserProfileDataValidatorImplTest {

    @Test
    public void testValidateRequest() {

        Set<RoleName> rolesData = new HashSet<>();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-organisation-manager");
        rolesData.add(roleName1);
        rolesData.add(roleName2);

        Set<RoleName> rolesToDeleteData = new HashSet<>();
        RoleName roleToDeleteName = new RoleName("pui-finance-manager");
        rolesToDeleteData.add(roleToDeleteName);

        ModifyUserProfileData modifyUserProfileData =
                        new ModifyUserProfileData("test@test.com","fname","lname", IdamStatus.ACTIVE.name(),rolesData,rolesToDeleteData);

        ModifyUserProfileDataValidator sut = new ModifyUserProfileDataValidatorImpl();
        ModifyUserProfileData actualModifyProfileData = sut.validateRequest(modifyUserProfileData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isNull();
        assertThat(actualModifyProfileData.getRolesAdd()).containsOnly(roleName1, roleName2);
        assertThat(actualModifyProfileData.getRolesDelete()).containsOnly(roleToDeleteName);

    }

    @Test
    public void testValidateRequestforStatus() {

        ModifyUserProfileData modifyUserProfileData =
                    new ModifyUserProfileData("test@test.com","fname","lname", IdamStatus.ACTIVE.name(),null,null);

        ModifyUserProfileDataValidator sut = new ModifyUserProfileDataValidatorImpl();
        ModifyUserProfileData actualModifyProfileData = sut.validateRequest(modifyUserProfileData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isEqualTo("ACTIVE");
        assertThat(actualModifyProfileData.getRolesAdd()).isNull();
        assertThat(actualModifyProfileData.getRolesDelete()).isNull();

    }

    @Test
    public void testValidateRequestforRoles() {

        Set<RoleName> rolesData = new HashSet<>();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-organisation-manager");
        rolesData.add(roleName1);
        rolesData.add(roleName2);

        Set<RoleName> rolesToDeleteData = new HashSet<>();
        RoleName roleToDeleteName = new RoleName("pui-finance-manager");
        rolesToDeleteData.add(roleToDeleteName);


        ModifyUserProfileData modifyUserProfileData =
                new ModifyUserProfileData("test@test.com", "fname", "lname", null, rolesData, rolesToDeleteData);

        ModifyUserProfileDataValidator sut = new ModifyUserProfileDataValidatorImpl();
        ModifyUserProfileData actualModifyProfileData = sut.validateRequest(modifyUserProfileData);
        assertThat(actualModifyProfileData).isNotNull();
        assertThat(actualModifyProfileData.getEmail()).isNull();
        assertThat(actualModifyProfileData.getIdamStatus()).isNull();
        assertThat(actualModifyProfileData.getRolesAdd()).containsOnly(roleName1, roleName2);
        assertThat(actualModifyProfileData.getRolesDelete()).containsOnly(roleToDeleteName);
    }

    @Test (expected =  InvalidRequest.class)
    public void testThrowErrorIfValidateRequestIsEmpty() {

        ModifyUserProfileData modifyUserProfileData =
                new ModifyUserProfileData("test@test.com", "fname", "lname", null, null, null);

        ModifyUserProfileDataValidator sut = new ModifyUserProfileDataValidatorImpl();
        ModifyUserProfileData actualModifyProfileData = sut.validateRequest(modifyUserProfileData);
    }


}