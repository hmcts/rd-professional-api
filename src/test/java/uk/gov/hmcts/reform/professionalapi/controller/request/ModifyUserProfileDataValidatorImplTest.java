package uk.gov.hmcts.reform.professionalapi.controller.request;

import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class ModifyUserProfileDataValidatorImplTest {

        //@Test
//not yet implemented (tdd)
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
}