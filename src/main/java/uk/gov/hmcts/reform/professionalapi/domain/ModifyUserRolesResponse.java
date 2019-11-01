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
    private RoleAdditionResponse addRolesResponse;
    private List<RoleDeletionResponse> deleteRolesResponse;

    public ModifyUserRolesResponse(ErrorResponse errorResponse, RoleAdditionResponse addRolesResponse, List<RoleDeletionResponse> deleteResponses) {
        this.errorResponse = errorResponse;
        this.addRolesResponse = addRolesResponse;
        this.deleteRolesResponse = deleteResponses;
    }

}

