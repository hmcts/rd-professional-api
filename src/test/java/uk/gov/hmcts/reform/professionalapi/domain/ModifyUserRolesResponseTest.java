package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class ModifyUserRolesResponseTest {

    @Test
    public void test_should_Return_User_profile_Response() {
        ModifyUserRolesResponse userProfileRolesResponse = new ModifyUserRolesResponse();
        userProfileRolesResponse.setRoleAdditionResponse(addRolesForUser());
        userProfileRolesResponse.setRoleDeletionResponse(deleteRolesForUser());

        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamMessage()).isEqualTo("Success");
        assertThat(userProfileRolesResponse.getRoleDeletionResponse().get(0).getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getRoleDeletionResponse().get(0).getIdamMessage()).isEqualTo("Success");
    }

    @Test
    public void test_modifyUserRolesResponse() {
        StatusUpdateResponse statusUpdateResponse = new StatusUpdateResponse();
        statusUpdateResponse.setIdamMessage("updateMessage");

        RoleAdditionResponse addRoleResponse = new RoleAdditionResponse();
        addRoleResponse.setIdamMessage("addMessage");

        RoleDeletionResponse deleteRoleResponse = new RoleDeletionResponse();
        deleteRoleResponse.setIdamMessage("deleteMessage");

        List<RoleDeletionResponse> deleteResponses = new ArrayList<>();
        deleteResponses.add(deleteRoleResponse);

        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse(addRoleResponse, deleteResponses, statusUpdateResponse);

        assertThat(modifyUserRolesResponse.getRoleAdditionResponse().getIdamMessage()).isEqualTo("addMessage");
        assertThat(modifyUserRolesResponse.getStatusUpdateResponse().getIdamMessage()).isEqualTo("updateMessage");
        assertThat(modifyUserRolesResponse.getRoleDeletionResponse().get(0).getIdamMessage()).isEqualTo("deleteMessage");
    }

    private RoleAdditionResponse addRolesForUser() {
        RoleAdditionResponse addRoleResponse = new RoleAdditionResponse();
        addRoleResponse.setIdamStatusCode("200");
        addRoleResponse.setIdamMessage("Success");
        return addRoleResponse;
    }

    private List<RoleDeletionResponse> deleteRolesForUser() {

        List<RoleDeletionResponse> deleteResponses = new ArrayList<>();
        RoleDeletionResponse deleteRoleResponse = new RoleDeletionResponse();
        deleteRoleResponse.setIdamStatusCode("200");
        deleteRoleResponse.setIdamMessage("Success");
        deleteResponses.add(deleteRoleResponse);
        return deleteResponses;
    }
}