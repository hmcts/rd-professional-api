package uk.gov.hmcts.reform.professionalapi.helper;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.Collections;
import java.util.HashSet;

import java.util.Set;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

public class OrganisationFixtures {

    private OrganisationFixtures() {
    }

    public OrganisationCreationRequest.OrganisationCreationRequestBuilder someMinimalOrganisationRequest() {

        return anOrganisationCreationRequest()
                .name("some-org-name")
                .status("ACTIVE")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(randomAlphanumeric(7).concat("@hmcts.net"))
                        .build())
                .contactInformation(Collections.singletonList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build()));
    }

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder whiteSpaceTrimOrganisationRequest() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        return anOrganisationCreationRequest()
                .name("  some-  org -name  ")
                .companyNumber(randomAlphabetic(8))
                .superUser(aUserCreationRequest()
                        .firstName(" some-fname    b    ")
                        .lastName(" some-         lname  ")
                        .email(" someone@somewhere.com ")
                        .build())
                .contactInformation(Collections.singletonList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build()));


    }

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder organisationRequestWithAllFields() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        return anOrganisationCreationRequest()
            .name("some-org-name")
            .status("PENDING")
            .sraId("sra-id")
            .sraRegulated("false")
            .companyUrl("company -url")
            .companyNumber(randomAlphabetic(8))
            .paymentAccount(paymentAccounts)
            .superUser(aUserCreationRequest()
                .firstName("some-fname")
                .lastName("some-lname")
                .email("someone@somewhere.com")
                .build())
            .contactInformation(Collections.singletonList(aContactInformationCreationRequest()
                    .addressLine1("addressLine1")
                    .addressLine2("addressLine2")
                    .addressLine3("addressLine3")
                    .country("country")
                    .county("county")
                    .townCity("town-city")
                    .postCode("some-post-code")
                    .dxAddress(Collections.singletonList(dxAddressCreationRequest()
                            .dxNumber("DX 1234567890")
                            .dxExchange("dxExchange").build()))
                    .build()));
    }

    public static OrganisationCreationRequest
            .OrganisationCreationRequestBuilder organisationRequestWithAllFieldsAreUpdated() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        return anOrganisationCreationRequest()
                .name("some-org-name1")
                .status("ACTIVE")
                .sraId("sra-id1")
                .sraRegulated("true")
                .companyUrl("company-url1")
                .companyNumber(randomAlphabetic(8))
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                .firstName("somefname")
                .lastName("somelname")
                .email("someone1@somewhere.com")
                .build())
            .contactInformation(Collections.singletonList(aContactInformationCreationRequest()
                    .addressLine1("addressLine3")
                    .addressLine2("addressLine4")
                    .addressLine3("addressLine5")
                    .country("some-country1")
                    .county("some-county1")
                    .townCity("som1-town-city")
                    .postCode("som1-post-code")
                    .dxAddress(Collections.singletonList(dxAddressCreationRequest()
                            .dxNumber("NI 1234567890")
                            .dxExchange("dxExchange1").build()))
                    .build()));
    }
}
