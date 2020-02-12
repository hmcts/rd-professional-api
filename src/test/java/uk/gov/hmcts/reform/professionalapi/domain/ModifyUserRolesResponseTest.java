package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;

public class ModifyUserRolesResponseTest {

    @Test
    public void should_Return_User_profile_Response() {

        ModifyUserRolesResponse userProfileRolesResponse = new ModifyUserRolesResponse();
        userProfileRolesResponse.setRoleAdditionResponse(addRolesForUser());
        userProfileRolesResponse.setRoleDeletionResponse(deleteRolesForUser());
        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamMessage()).isEqualTo("Success");
        assertThat(userProfileRolesResponse.getRoleDeletionResponse().get(0).getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getRoleDeletionResponse().get(0).getIdamMessage()).isEqualTo("Success");
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

    @Test
    public void modifyUserRolesResponseTest() {
        StatusUpdateResponse statusUpdateResponse = new StatusUpdateResponse();
        statusUpdateResponse.setIdamMessage("updateMessage");
        RoleAdditionResponse addRoleResponse = new RoleAdditionResponse();
        addRoleResponse.setIdamMessage("addMessage");
        List<RoleDeletionResponse> deleteResponses = new ArrayList<>();
        RoleDeletionResponse deleteRoleResponse = new RoleDeletionResponse();
        deleteRoleResponse.setIdamMessage("deleteMessage");
        deleteResponses.add(deleteRoleResponse);
        ErrorResponse errorResponse = new ErrorResponse("failure", "500", "1200");
        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse(errorResponse, addRoleResponse, deleteResponses, statusUpdateResponse);

        assertThat(modifyUserRolesResponse.getRoleAdditionResponse().getIdamMessage()).isEqualTo("addMessage");
        assertThat(modifyUserRolesResponse.getStatusUpdateResponse().getIdamMessage()).isEqualTo("updateMessage");
        assertThat(modifyUserRolesResponse.getRoleDeletionResponse().get(0).getIdamMessage()).isEqualTo("deleteMessage");
        assertThat(modifyUserRolesResponse.getErrorResponse().getErrorMessage()).isEqualTo("failure");
        assertThat(modifyUserRolesResponse.getErrorResponse().getErrorDescription()).isEqualTo("500");
    }
}