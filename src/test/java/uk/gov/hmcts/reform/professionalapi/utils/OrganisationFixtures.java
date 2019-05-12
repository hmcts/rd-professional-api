package uk.gov.hmcts.reform.professionalapi.utils;

import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.Arrays;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;


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

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder organisationRequestWithAllFields() {

        return anOrganisationCreationRequest()
            .name("some-org-name")
            .status(OrganisationStatus.PENDING)
            .sraId("sra-id")
            .sraRegulated(Boolean.FALSE)
            .companyUrl("company-url1")
            .companyNumber("company1")
            .pbaAccounts(Arrays.asList(aPbaPaymentAccount()
                .pbaNumber("pbaNumber-1")
                .build()))
            .superUser(aUserCreationRequest()
                .firstName("some-fname")
                .lastName("some-lname")
                .email("someone@somewhere.com")
                .build())
            .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("some-country")
                .county("some-county")
                .townCity("some-town-city")
                .postCode("some-post-code")
                .dxAddress(Arrays.asList(dxAddressCreationRequest()
                    .dxNumber("DX 1234567890")
                    .dxExchange("dxExchange").build()))
                .build()));
    }

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder organisationRequestWithAllFieldsAreUpdated() {

        return anOrganisationCreationRequest()
            .name("some-org-name1")
            .status(OrganisationStatus.ACTIVE)
            .sraId("sra-id1")
            .sraRegulated(Boolean.TRUE)
            .companyUrl("company-url1")
            .companyNumber("company1")
            .pbaAccounts(Arrays.asList(aPbaPaymentAccount()
                .pbaNumber("pbaNumber-1")
                .build()))
            .superUser(aUserCreationRequest()
                .firstName("some-fname")
                .lastName("some-lname")
                .email("someone@somewhere.com")
                .build())
            .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine3")
                .addressLine2("addressLine4")
                .addressLine3("addressLine5")
                .country("some-country1")
                .county("some-county1")
                .townCity("som1-town-city")
                .postCode("som1-post-code")
                .dxAddress(Arrays.asList(dxAddressCreationRequest()
                    .dxNumber("NI 1234567890")
                    .dxExchange("dxExchange1").build()))
                .build()));
    }
}
