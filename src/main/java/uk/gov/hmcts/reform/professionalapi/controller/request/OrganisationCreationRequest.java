package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;

@Getter
@Setter
@Builder(builderMethodName = "anOrganisationCreationRequest")
public class OrganisationCreationRequest {

    @NotNull
    private final String name;

    private String status;

    private final String sraId;

    private String sraRegulated;

    private final String companyNumber;

    private final String companyUrl;

    @NotNull
    private final UserCreationRequest superUser;

    private Set<String> paymentAccount;

    @NotNull
    private List<ContactInformationCreationRequest> contactInformation;

    @NotNull
    private MFAStatus mfa;

    @JsonCreator
    public OrganisationCreationRequest(
            @JsonProperty("name") String name,
            @JsonProperty("status") String status,
            @JsonProperty("sraId") String sraId,
            @JsonProperty("sraRegulated") String sraRegulated,
            @JsonProperty("companyNumber") String companyNumber,
            @JsonProperty("companyUrl") String companyUrl,
            @JsonProperty("superUser") UserCreationRequest superUser,
            @JsonProperty("paymentAccount") Set<String> paymentAccount,
            @JsonProperty("contactInformation") List<ContactInformationCreationRequest> contactInformationRequest,
            @JsonProperty("mfa") MFAStatus mfa) {

        this.name = name;
        this.status = status;
        this.sraId = sraId;
        this.sraRegulated = sraRegulated;
        this.companyNumber = companyNumber;
        this.companyUrl = companyUrl;
        this.superUser = superUser;
        this.paymentAccount = paymentAccount;
        this.contactInformation = contactInformationRequest;
        this.mfa = mfa;
    }
}