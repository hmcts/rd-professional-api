package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class OrganisationOtherOrgsCreationRequest extends OrganisationCreationRequest {

    private final String orgTypeKey;

    private List<OrgAttributeRequest> orgAttributes;

    public OrganisationOtherOrgsCreationRequest(String name, String status, String statusMessage, String sraId,
                                                String sraRegulated, String companyNumber, String companyUrl,
                                                UserCreationRequest superUser, Set<String> paymentAccount,
                                                List<ContactInformationCreationRequest> contactInformationRequest,
                                                String orgTypeKey, List<OrgAttributeRequest> orgAttributes
                                                ) {
        super(name, status, statusMessage, sraId, sraRegulated, companyNumber, companyUrl, superUser, paymentAccount,
                contactInformationRequest);

        this.orgTypeKey = orgTypeKey;
        this.orgAttributes = orgAttributes;

    }
}
