package uk.gov.hmcts.reform.professionalapi.helper;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.Arrays;
import java.util.HashSet;

import java.util.Set;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

public class OrganisationFixtures {

    private OrganisationFixtures() {
    }

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder someMinimalOrganisationRequest() {

        return anOrganisationCreationRequest()
                .name("some-org-name")
                .status("ACTIVE")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(randomAlphanumeric(7).concat("@test.com"))
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1")
                        .uprn("uprn")
                        .build()));
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
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
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
            .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("country")
                .county("county")
                .townCity("town-city")
                .uprn("uprn")
                .postCode("some-post-code")
                    .dxAddress(Arrays.asList(dxAddressCreationRequest()
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

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder
        organisationRequestWithMultipleAddressAllFields() {
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
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country1")
                        .county("county2")
                        .townCity("town-city1")
                        .uprn("uprn1")
                        .postCode("post-code1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange1").build()))
                        .build(),
                        aContactInformationCreationRequest()
                                .addressLine1("addressLine9")
                                .addressLine2("addressLine8")
                                .addressLine3("addressLine7")
                                .country("country2")
                                .county("county2")
                                .townCity("town-city2")
                                .uprn("uprn2")
                                .postCode("post-code2")
                                .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                        .dxNumber("DX 2345678901")
                                        .dxExchange("dxExchange2").build()))
                                .build(),
                        aContactInformationCreationRequest()
                                .addressLine1("addressLine5")
                                .addressLine2("addressLine4")
                                .addressLine3("addressLine3")
                                .country("country3")
                                .county("county3")
                                .townCity("town-city3")
                                .uprn("uprn3")
                                .postCode("post-code3")
                                .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                        .dxNumber("DX 3456789012")
                                        .dxExchange("dxExchange3").build()))
                                .build()
                ));
    }
}
