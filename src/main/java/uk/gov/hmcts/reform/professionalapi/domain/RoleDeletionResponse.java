package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDeletionResponse {

    private String roleName;
    private String idamStatusCode;
    private String idamMessage;

}
