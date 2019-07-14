package uk.gov.hmcts.reform.professionalapi.controller.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class UserProfile {

    private UUID idamId;
    private String email;
    private String firstName;
    private String lastName;
    private IdamStatus idamStatus;
}
