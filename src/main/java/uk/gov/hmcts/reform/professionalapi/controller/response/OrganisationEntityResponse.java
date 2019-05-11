package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@NoArgsConstructor
public class OrganisationEntityResponse  {

    @JsonProperty
    private   String id;
    @JsonProperty
    private   String name;
    @JsonProperty
    private OrganisationStatus status;
    @JsonProperty
    private   String sraId;
    @JsonProperty
    private   Boolean sraRegulated;
    @JsonProperty
    private   String companyNumber;
    @JsonProperty
    private   String companyUrl;
    @JsonProperty
    private   List<SuperUserResponse> superUser;
    @JsonProperty
    private  List<PbaAccountResponse> pbaAccounts;
    @JsonProperty
    private  List<ContactInformationResponse> contactInformation;

    public OrganisationEntityResponse(Organisation organisation) {

        this.id = organisation.getId().toString();
        this.name = organisation.getName();
        this.status = organisation.getStatus();
        this.sraId = organisation.getSraId();
        this.sraRegulated = organisation.getSraRegulated();
        this.companyNumber = organisation.getCompanyNumber();
        this.companyUrl = organisation.getCompanyUrl();
        this.superUser = organisation.getUsers()
                .stream()
                .map(user -> new SuperUserResponse(user))
                .collect(toList());
        this.pbaAccounts = organisation.getPaymentAccounts()
                .stream()
                .map(pbaAccount -> new PbaAccountResponse(pbaAccount))
                .collect(toList());
        this.contactInformation = organisation.getContactInformation()
                                   .stream()
                                   .map(contactInfo -> new ContactInformationResponse(contactInfo))
                                   .collect(toList());
    }
}
