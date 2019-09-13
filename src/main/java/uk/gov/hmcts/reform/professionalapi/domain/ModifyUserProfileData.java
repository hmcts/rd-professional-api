package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ModifyUserProfileData {

    private String email;

    private String firstName;

    private String lastName;

    private String idamStatus;

    private Set<RoleName> rolesAdd;

    @JsonCreator
    public ModifyUserProfileData(@JsonProperty(value = "email") String email,
                                 @JsonProperty(value = "firstName") String firstName,
                                 @JsonProperty(value = "lastName") String lastName,
                                 @JsonProperty(value = "idamStatus") String idamStatus,
                                 @JsonProperty(value = "rolesAdd") Set<RoleName> rolesAdd
    ) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idamStatus = idamStatus;
        this.rolesAdd = rolesAdd;

    }

}
