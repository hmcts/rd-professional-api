package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Builder(builderMethodName = "anOrganisationCreationRequest")
public class OrganisationCreationRequest {

    @NotNull
    private final String name;

    private final OrganisationStatus status;

    private final String sraId;

    private final Boolean sraRegulated;

    private final String companyNumber;

    private final String companyUrl;

    @NotNull
    private final UserCreationRequest superUser;

    private List<PbaAccountCreationRequest> pbaAccounts;

    private List<ContactInformationCreationRequest> contactInformation;

    @JsonCreator
    public OrganisationCreationRequest(
            @JsonProperty("name") String name,
            @JsonProperty("status") OrganisationStatus status,
            @JsonProperty("sraId") String sraId,
            @JsonProperty("sraRegulated") Boolean sraRegulated,
            @JsonProperty("companyNumber") String companyNumber,
            @JsonProperty("companyUrl") String companyUrl,
            @JsonProperty("superUser") UserCreationRequest superUser,
            @JsonProperty("pbaAccounts") List<PbaAccountCreationRequest> pbaAccountCreationRequests,
            @JsonProperty("contactInformation") List<ContactInformationCreationRequest> contactInformationRequest) {

        this.name = name;
        this.status = status;
        this.sraId = sraId;
        this.sraRegulated = sraRegulated;
        this.companyNumber = companyNumber;
        this.companyUrl = companyUrl;
        this.superUser = superUser;
        this.pbaAccounts = pbaAccountCreationRequests;
        this.contactInformation = contactInformationRequest;
    }

    public String getName() {
        return name;
    }

    public String getSraId() {
        return sraId;
    }

    public Boolean getSraRegulated() {
        return sraRegulated;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getCompanyUrl() {
        return companyUrl;
    }

    public UserCreationRequest getSuperUser() {
        return superUser;
    }

    public List<PbaAccountCreationRequest> getPbaAccounts() {
        return pbaAccounts;
    }

    public List<ContactInformationCreationRequest> getContactInformation() {
        return contactInformation;
    }

}