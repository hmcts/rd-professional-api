package uk.gov.hmcts.reform.professionalapi.domain;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;

@Getter
@Setter
@NoArgsConstructor
public class ModifyUserRolesResponse {

    private ErrorResponse errorResponse;
    private AddRoleResponse addRolesResponse;
    private List<DeleteRoleResponse> deleteRolesResponse;

    public ModifyUserRolesResponse(ErrorResponse errorResponse, AddRoleResponse addRolesResponse, List<DeleteRoleResponse> deleteResponses) {
        this.errorResponse = errorResponse;
        this.addRolesResponse = addRolesResponse;
        this.deleteRolesResponse = deleteResponses;
    }

}

