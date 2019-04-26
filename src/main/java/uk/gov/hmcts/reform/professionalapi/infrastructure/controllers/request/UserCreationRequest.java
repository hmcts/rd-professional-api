package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "aUserCreationRequest")
public class UserCreationRequest {

    @NotNull
    private final String firstName;

    @NotNull
    private final String lastName;

    @NotNull
    private final String email;

    private final PbaAccountCreationRequest pbaAccount;


    @JsonCreator
    public UserCreationRequest(

            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String email,
            @JsonProperty("pbaAccount") PbaAccountCreationRequest pbaAccount) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.pbaAccount = pbaAccount;
    }


	public String getFirstName() {
		return firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public String getEmail() {
		return email;
	}


	public PbaAccountCreationRequest getPbaAccount() {
		return pbaAccount;
	}
}
