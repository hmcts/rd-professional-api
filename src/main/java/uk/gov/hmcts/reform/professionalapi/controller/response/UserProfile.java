package uk.gov.hmcts.reform.professionalapi.controller.response;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfile {

    private UUID idamId;
    private String email;
    private String firstName;
    private String lastName;
    private IdamStatus idamStatus;
}
