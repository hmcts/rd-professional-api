package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ModifyUserRolesResponseTest {

    @Test
    public void should_Return_User_profile_Resposne() {

        ModifyUserRolesResponse userProfileRolesResponse = new ModifyUserRolesResponse();
        userProfileRolesResponse.setAddRoleResponse(addRolesForUser());
        userProfileRolesResponse.setDeleteRolesResponse(deleteRolesForUser());
        assertThat(userProfileRolesResponse.getAddRoleResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getAddRoleResponse().getIdamMessage()).isEqualTo("Success");
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getIdamMessage()).isEqualTo("Success");
    }

    private AddRoleResponse addRolesForUser() {

        AddRoleResponse addRoleResponse = new AddRoleResponse();
        addRoleResponse.setIdamStatusCode("200");
        addRoleResponse.setIdamMessage("Success");
        return addRoleResponse;
    }

    private List<DeleteRoleResponse> deleteRolesForUser() {

        List<DeleteRoleResponse> deleteResponses = new ArrayList<>();
        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse();
        deleteRoleResponse.setIdamStatusCode("200");
        deleteRoleResponse.setIdamMessage("Success");
        deleteResponses.add(deleteRoleResponse);
        return deleteResponses;
    }
}