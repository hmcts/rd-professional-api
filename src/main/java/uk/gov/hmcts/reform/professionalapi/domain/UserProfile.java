package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;

@Getter
@AllArgsConstructor
public class UserProfile {

    private String idamId;
    private String email;
    private String firstName;
    private String lastName;
    private IdamStatus idamStatus;
}
