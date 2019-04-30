package uk.gov.hmcts.reform.professionalapi.utils;

import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrganisationFixtures {

	private OrganisationFixtures() {
	}

	public static OrganisationCreationRequest.OrganisationCreationRequestBuilder someMinimalOrganisationRequest() {

                return anOrganisationCreationRequest()
                		.name("some-org-name")
                		.superUser(aUserCreationRequest()
                				.firstName("some-fname")
                				.lastName("some-lname")
                				.email("someone@somewhere.com")
                				.build())
                		.contactInformation(Arrays.asList(aContactInformationCreationRequest()
                		.addressLine1("addressLine1").build()));
    }
}
