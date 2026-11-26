package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Jackson 3 may treat this as a creator; keep it for service mapping only (Boot 3 parity)
    @JsonIgnore
    public OrganisationMinimalInfoResponse(Organisation organisation, Boolean isAddressRequired) {

        getOrganisationMinimalInfoResponse(organisation, isAddressRequired);
    }

    public void getOrganisationMinimalInfoResponse(Organisation organisation, Boolean isAddressRequired) {
        this.name = organisation.getName();
        this.organisationIdentifier = organisation.getOrganisationIdentifier();

        if (Boolean.TRUE.equals(isAddressRequired)) {
            this.contactInformation = organisation.getContactInformation()
                    .stream()
                    .map(ContactInformationResponse::new)
                    .toList();
        }
    }
}