package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.professionalapi.domain.UserCategory;
import uk.gov.hmcts.reform.professionalapi.domain.UserType;

@Setter
@Getter
@Builder(builderMethodName = "anUserProfileCreationRequest")
public class UserProfileCreationRequest  {

    @NotBlank
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private LanguagePreference languagePreference;

    @NotBlank
    private UserCategory userCategory;

    @NotBlank
    private UserType userType;

    @NotEmpty
    private List<String> roles;

    @JsonCreator
    public UserProfileCreationRequest(@JsonProperty(value = "email") String email,
                                      @JsonProperty(value = "firstName") String firstName,
                                      @JsonProperty(value = "lastName") String lastName,
                                      @JsonProperty(value = "languagePreference") LanguagePreference languagePreference,
                                      @JsonProperty(value = "userCategory") UserCategory userCategory,
                                      @JsonProperty(value = "userType") UserType userType,
                                      @JsonProperty(value = "roles") List<String> roles) {

        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.languagePreference = languagePreference;
        this.userCategory = userCategory;
        this.userType = userType;
        this.roles = roles;
    }
}
