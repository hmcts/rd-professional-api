package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;
import java.util.List;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@NoArgsConstructor
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
    private List<PbaAccountResponse> pbaAccounts;
    @JsonProperty
    private List<ContactInformationResponse> contactInformation;

    public OrganisationEntityResponse(Organisation organisation, Boolean isRequiredAllEntities) {

        getOrganisationEntityResponse(organisation, isRequiredAllEntities);
    }

    private void getOrganisationEntityResponse(Organisation organisation, Boolean isRequiredAllEntities) {

        this.organisationIdentifier = StringUtils.isEmpty(organisation.getOrganisationIdentifier())
                ? "" : organisation.getOrganisationIdentifier().toString();
        this.name = organisation.getName();
        this.status = organisation.getStatus();
        this.sraId = organisation.getSraId();
        this.sraRegulated = organisation.getSraRegulated();
        this.companyNumber = organisation.getCompanyNumber();
        this.companyUrl = organisation.getCompanyUrl();
        this.superUser = getSuperUserFromUserList(organisation);
        this.pbaAccounts = organisation.getPaymentAccounts()
                .stream()
                .map(pbaAccount -> new PbaAccountResponse(pbaAccount))
                .collect(toList());
        if (isRequiredAllEntities) {
            this.contactInformation = organisation.getContactInformation()
                    .stream()
                    .map(contactInfo -> new ContactInformationResponse(contactInfo))
                    .collect(toList());
        }
    }

    private SuperUserResponse getSuperUserFromUserList(Organisation organisation) {
        ProfessionalUser user = organisation.getUsers().stream().sorted((Comparator.comparing(ProfessionalUser::getCreated))).findFirst().get();
        return new SuperUserResponse(user);
    }

}