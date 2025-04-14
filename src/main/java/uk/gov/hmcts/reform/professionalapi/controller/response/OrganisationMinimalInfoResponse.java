package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

import java.util.List;

@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganisationMinimalInfoResponse {

    @JsonProperty
    protected String name;
    @JsonProperty
    protected String organisationIdentifier;
    @JsonProperty
    protected List<ContactInformationResponse> contactInformation;

    public OrganisationMinimalInfoResponse(Organisation organisation, Boolean isAddressRequired) {

        getOrganisationMinimalInfoResponse(organisation, isAddressRequired);
    }

    public void getOrganisationMinimalInfoResponse(Organisation organisation, Boolean isAddressRequired) {
        this.name = organisation.getName();
        this.organisationIdentifier = organisation.getOrganisationIdentifier();

        if (Boolean.TRUE.equals(isAddressRequired)) {
            this.contactInformation = organisation.getContactInformations()
                    .stream()
                    .map(ContactInformationResponse::new)
                    .toList();
        }
    }
}