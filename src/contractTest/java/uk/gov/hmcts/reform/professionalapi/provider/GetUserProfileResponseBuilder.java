package uk.gov.hmcts.reform.professionalapi.provider;

import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;

import java.util.List;

public final class GetUserProfileResponseBuilder {
    private String idamId;
    private String firstName;
    private String lastName;
    private String email;
    private IdamStatus idamStatus;
    private List<String> roles;
    private String idamStatusCode;
    private String idamMessage;

    private GetUserProfileResponseBuilder() {
    }

    public static GetUserProfileResponseBuilder aGetUserProfileResponse() {
        return new GetUserProfileResponseBuilder();
    }

    public GetUserProfileResponseBuilder withIdamId(String idamId) {
        this.idamId = idamId;
        return this;
    }

    public GetUserProfileResponseBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public GetUserProfileResponseBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public GetUserProfileResponseBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public GetUserProfileResponseBuilder withIdamStatus(IdamStatus idamStatus) {
        this.idamStatus = idamStatus;
        return this;
    }

    public GetUserProfileResponseBuilder withRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public GetUserProfileResponseBuilder withIdamStatusCode(String idamStatusCode) {
        this.idamStatusCode = idamStatusCode;
        return this;
    }

    public GetUserProfileResponseBuilder withIdamMessage(String idamMessage) {
        this.idamMessage = idamMessage;
        return this;
    }

    public GetUserProfileResponse build() {
        GetUserProfileResponse getUserProfileResponse = new GetUserProfileResponse();
        getUserProfileResponse.setIdamId(idamId);
        getUserProfileResponse.setFirstName(firstName);
        getUserProfileResponse.setLastName(lastName);
        getUserProfileResponse.setEmail(email);
        getUserProfileResponse.setIdamStatus(idamStatus);
        getUserProfileResponse.setRoles(roles);
        getUserProfileResponse.setIdamStatusCode(idamStatusCode);
        getUserProfileResponse.setIdamMessage(idamMessage);
        return getUserProfileResponse;
    }
}
