package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public class OrganisationEntityResponse  {

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
        System.out.println("orgentityresponse, super user start");
        if (!organisation.getUsers().isEmpty()) {
            this.superUser = new SuperUserResponse(organisation.getUsers().get(0));
        }
        System.out.println("orgentityresponse, super user end");
        System.out.println("orgentityresponse, payment account start");
        this.paymentAccount = organisation.getPaymentAccounts()
                .stream()
                .map(pbaAccount -> new PbaAccountResponse(pbaAccount).getPbaNumber())
                .collect(toList());
        System.out.println("orgentityresponse, payment account end");
        System.out.println("orgentityresponse, contact info start, this gets the dx address tho?");
        if (Boolean.TRUE.equals(isRequiredAllEntities)) {
            this.contactInformation = organisation.getContactInformation()
                    .stream()
                    .map(contactInfo -> new ContactInformationResponse(contactInfo))
                    .collect(toList());
            System.out.println("orgentityresponse, contact info end, this gets the dx address tho?");
        }
    }
}