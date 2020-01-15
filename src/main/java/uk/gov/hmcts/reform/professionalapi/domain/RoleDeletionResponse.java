package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleDeletionResponse {

    private String roleName;
    private String idamStatusCode;
    private String idamMessage;

    public RoleDeletionResponse(String roleName, String idamStatusCode, String idamMessage) {
        this.roleName = roleName;
        this.idamStatusCode = idamStatusCode;
        this.idamMessage = idamMessage;
    }

}
