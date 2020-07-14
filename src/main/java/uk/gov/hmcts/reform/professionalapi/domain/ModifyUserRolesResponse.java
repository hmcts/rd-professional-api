package uk.gov.hmcts.reform.professionalapi.domain;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModifyUserRolesResponse {

    private RoleAdditionResponse roleAdditionResponse;
    private List<RoleDeletionResponse> roleDeletionResponse;
    private StatusUpdateResponse statusUpdateResponse;

    public ModifyUserRolesResponse(RoleAdditionResponse addRolesResponse, List<RoleDeletionResponse> deleteResponses, StatusUpdateResponse statusUpdateResponse) {
        this.roleAdditionResponse = addRolesResponse;
        this.roleDeletionResponse = deleteResponses;
        this.statusUpdateResponse = statusUpdateResponse;
    }

}

