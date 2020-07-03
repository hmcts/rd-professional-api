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
    private RoleAdditionResponse roleAdditionResponse;
    private List<RoleDeletionResponse> roleDeletionResponse;
    private StatusUpdateResponse statusUpdateResponse;

    public ModifyUserRolesResponse(ErrorResponse errorResponse, RoleAdditionResponse addRolesResponse,
                                   List<RoleDeletionResponse> deleteResponses,
                                   StatusUpdateResponse statusUpdateResponse) {
        this.errorResponse = errorResponse;
        this.roleAdditionResponse = addRolesResponse;
        this.roleDeletionResponse = deleteResponses;
        this.statusUpdateResponse = statusUpdateResponse;
    }

}

