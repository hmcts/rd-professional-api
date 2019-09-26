package uk.gov.hmcts.reform.professionalapi.domain;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModifyUserRolesResponse {

    private AddRoleResponse addRoleResponse;
    private List<DeleteRoleResponse> deleteRolesResponse;

    public ModifyUserRolesResponse(AddRoleResponse addRoleResponse, List<DeleteRoleResponse> deleteResponses) {
        this.addRoleResponse = addRoleResponse;
        this.deleteRolesResponse = deleteResponses;
    }

}

