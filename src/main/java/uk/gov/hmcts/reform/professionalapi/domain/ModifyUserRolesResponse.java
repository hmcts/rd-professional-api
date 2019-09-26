package uk.gov.hmcts.reform.professionalapi.domain;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModifyUserRolesResponse {

    private AddRoleResponse addRolesResponse;
    private List<DeleteRoleResponse> deleteRolesResponse;

    public ModifyUserRolesResponse(AddRoleResponse addRolesResponse, List<DeleteRoleResponse> deleteResponses) {
        this.addRolesResponse = addRolesResponse;
        this.deleteRolesResponse = deleteResponses;
    }

}

