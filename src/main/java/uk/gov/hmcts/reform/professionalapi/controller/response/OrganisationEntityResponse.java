package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@NoArgsConstructor
public class OrganisationEntityResponse  {

    private PbaAccountResponse pbaAccountResponse;

    @JsonProperty
    private String organisationIdentifier;
    @JsonProperty
    private String name;
    @JsonProperty
    private OrganisationStatus status;
    @JsonProperty
    private String sraId;
    @JsonProperty
    private Boolean sraRegulated;
    @JsonProperty
    private String companyNumber;
    @JsonProperty
    private String companyUrl;
    @JsonProperty
    private SuperUserResponse superUser;
    @JsonProperty
    private List<String> paymentAccount;
    @JsonProperty
    private List<ContactInformationResponse> contactInformation;

    public OrganisationEntityResponse(Organisation organisation, Boolean isRequiredAllEntities) {

        getOrganisationEntityResponse(organisation, isRequiredAllEntities);
    }

    private void getOrganisationEntityResponse(Organisation organisation, Boolean isRequiredAllEntities) {

        this.organisationIdentifier = StringUtils.isEmpty(organisation.getOrganisationIdentifier())
                ? "" : organisation.getOrganisationIdentifier();
        this.name = organisation.getName();
        this.status = organisation.getStatus();
        this.sraId = organisation.getSraId();
        this.sraRegulated = organisation.getSraRegulated();
        this.companyNumber = organisation.getCompanyNumber();
        this.companyUrl = organisation.getCompanyUrl();
        if (organisation.getUsers().size() > 0) {
            this.superUser = new SuperUserResponse(organisation.getUsers().get(0));
        }
        this.paymentAccount = organisation.getPaymentAccounts()
                .stream()
                .map(pbaAccount -> new PbaAccountResponse(pbaAccount).getPbaNumber())
                .collect(toList());
        if (isRequiredAllEntities) {
            this.contactInformation = organisation.getContactInformation()
                    .stream()
                    .map(contactInfo -> new ContactInformationResponse(contactInfo))
                    .collect(toList());
        }
    }

}