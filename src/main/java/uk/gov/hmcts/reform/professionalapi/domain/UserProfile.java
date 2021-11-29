package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;

@Getter
@AllArgsConstructor
public class UserProfile {

    private final String idamId;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final IdamStatus idamStatus;
}
