package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfile {

    private String idamId;
    private String email;
    private String firstName;
    private String lastName;
    private IdamStatus idamStatus;
}
