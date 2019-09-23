package uk.gov.hmcts.reform.professionalapi.controller.response;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileCreationResponse {

    public void setIdamRegistrationResponse(Integer idamRegistrationResponse) {
        this.idamRegistrationResponse = idamRegistrationResponse;
    }

    private UUID idamId;
    private Integer idamRegistrationResponse;

    public boolean isUserCreated() {
        return getIdamRegistrationResponse() == HttpStatus.CREATED.value();
    }
}
