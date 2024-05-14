package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Getter
@Setter
@Builder(builderMethodName = "userUpdateRequest")
public class UserUpdateRequest {

    private String existingAdminEmail;

    private String newAdminEmail;


    @JsonCreator
    public UserUpdateRequest(
            @JsonProperty("existingAdminEmail") String existingAdminEmail,
            @JsonProperty("newAdminEmail") String newAdminEmail) {

        if (isNotBlank(newAdminEmail)) {

            this.newAdminEmail = newAdminEmail.toLowerCase().trim();
        }
        if (isNotBlank(existingAdminEmail)) {

            this.existingAdminEmail = existingAdminEmail.toLowerCase().trim();
        }
    }


}

