package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

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
        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(organisation.getName()))) {

            this.name = organisation.getName().trim();
        }
        this.status = organisation.getStatus();

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(organisation.getSraId()))) {

            this.sraId = organisation.getSraId().trim();
        }

        this.sraRegulated = organisation.getSraRegulated();

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(organisation.getCompanyNumber()))) {

            this.companyNumber = organisation.getCompanyNumber().trim();
        }

        if (!StringUtils.isEmpty(PbaAccountUtil.removeEmptySpaces(organisation.getCompanyUrl()))) {

            this.companyUrl = organisation.getCompanyUrl().trim();
        }

        if (!organisation.getUsers().isEmpty()) {

            this.superUser = new SuperUserResponse(organisation.getUsers().get(0));
        }
        if (!CollectionUtils.isEmpty(organisation.getPaymentAccounts())) {

            List<String> pbaNumbers = removeEmptyStringFromList(organisation.getPaymentAccounts()
                    .stream()
                    .map(pbaAccount -> new PbaAccountResponse(pbaAccount).getPbaNumber())
                    .collect(toList()));
            if (!CollectionUtils.isEmpty(pbaNumbers)) {

                this.paymentAccount = pbaNumbers;
            }

        }

        if (isRequiredAllEntities && !CollectionUtils.isEmpty(organisation.getContactInformation())) {
            this.contactInformation = organisation.getContactInformation()
                    .stream()
                    .map(contactInfo -> new ContactInformationResponse(contactInfo))
                    .collect(toList());
        }
    }

    private List<String> removeEmptyStringFromList(List<String> values) {

        List<String> modifiedValues;

        return modifiedValues = values.stream().filter(value -> !value.trim().isEmpty()).collect(Collectors.toList());
    }

}