package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserProfileUpdatedData {

    private String idamId;

    private String email;

    private String firstName;

    private String lastName;

    private String idamStatus;

    private Set<RoleName> rolesAdd;

    private Set<RoleName> rolesDelete;

    @JsonInclude(NON_NULL)
    private Set<UserAccessType> userAccessTypes;

    @JsonCreator
    public UserProfileUpdatedData(@JsonProperty(value = "idamId") String idamId,
                                  @JsonProperty(value = "email") String email,
                                  @JsonProperty(value = "firstName") String firstName,
                                  @JsonProperty(value = "lastName") String lastName,
                                  @JsonProperty(value = "idamStatus") String idamStatus,
                                  @JsonProperty(value = "rolesAdd") Set<RoleName> rolesAdd,
                                  @JsonProperty(value = "rolesDelete") Set<RoleName> rolesDelete,
                                  @JsonProperty(value = "userAccessTypes") Set<UserAccessType> userAccessTypes
    ) {
        this.idamId = idamId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idamStatus = idamStatus;
        this.rolesAdd = rolesAdd;
        this.rolesDelete = rolesDelete;
        this.userAccessTypes = userAccessTypes;
    }

}
