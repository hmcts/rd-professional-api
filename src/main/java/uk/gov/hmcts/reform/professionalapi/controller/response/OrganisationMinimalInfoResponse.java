package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

import java.util.List;

import static java.util.stream.Collectors.toList;

@NoArgsConstructor
@Getter
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

    private void getOrganisationMinimalInfoResponse(Organisation organisation, Boolean isAddressRequired) {
        this.name = organisation.getName();
        this.organisationIdentifier = organisation.getOrganisationIdentifier();

        if (Boolean.TRUE.equals(isAddressRequired)) {
            this.contactInformation = organisation.getContactInformation()
                    .stream()
                    .map(ContactInformationResponse::new)
                    .collect(toList());
        }
    }
}