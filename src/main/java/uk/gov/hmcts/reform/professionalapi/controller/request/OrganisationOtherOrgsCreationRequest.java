package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class OrganisationOtherOrgsCreationRequest extends OrganisationCreationRequest {

    private  String orgType;

    private List<OrgAttributeRequest> orgAttributes;

    @JsonCreator
    public OrganisationOtherOrgsCreationRequest(@JsonProperty("name") String name,
                @JsonProperty("status") String status,
                @JsonProperty("statusMessage") String statusMessage,
                @JsonProperty("sraId") String sraId,
                @JsonProperty("sraRegulated") String sraRegulated,
                @JsonProperty("companyNumber") String companyNumber,
                @JsonProperty("companyUrl") String companyUrl,
                @JsonProperty("superUser") UserCreationRequest superUser,
                @JsonProperty("paymentAccount") Set<String> paymentAccount,
                @JsonProperty("contactInformation") List<ContactInformationCreationRequest> contactInformationRequest,
                @JsonProperty("orgType") String orgType,
                @JsonProperty("orgAttributes") List<OrgAttributeRequest> orgAttributes) {
        super(name, status, statusMessage, sraId, sraRegulated, companyNumber, companyUrl, superUser, paymentAccount,
                contactInformationRequest);

        this.orgType = orgType;
        this.orgAttributes = orgAttributes;
    }
}
