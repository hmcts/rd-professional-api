package uk.gov.hmcts.reform.professionalapi.helper;

import org.apache.commons.lang.RandomStringUtils;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

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

    public static OrganisationOtherOrgsCreationRequest otherOrganisationRequestWithAllFields() {

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();

        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();

        orgAttributeRequest.setKey("testKey");
        orgAttributeRequest.setValue("testValue");

        orgAttributeRequests.add(orgAttributeRequest);

        OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest =
                new OrganisationOtherOrgsCreationRequest("some-org-name",
                        "PENDING",
                        "test",
                        "sra-id",
                        "false",
                        "comNum",
                        "company-url",
                        aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email("someone@somewhere.com")
                                .build(),
                        paymentAccounts,
                        Collections
                                .singletonList(aContactInformationCreationRequest()
                                        .addressLine1("addressLine1")
                                        .addressLine2("addressLine2")
                                        .addressLine3("addressLine3")
                                        .country("country")
                                        .county("county")
                                        .townCity("town-city")
                                        .uprn("uprn")
                                        .postCode("some-post-code")
                                        .dxAddress(Collections
                                                .singletonList(dxAddressCreationRequest()
                                                        .dxNumber("DX 1234567890")
                                                        .dxExchange("dxExchange").build()))
                                        .build()),
                        "Doctor",
                        orgAttributeRequests);

        return organisationOtherOrgsCreationRequest;

    }

    public static OrganisationOtherOrgsCreationRequest otherOrganisationRequestWithAllFieldsAreUpdated() {

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();

        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();

        orgAttributeRequest.setKey("testKey1");
        orgAttributeRequest.setValue("testValue1");

        orgAttributeRequests.add(orgAttributeRequest);

        OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest =
                new OrganisationOtherOrgsCreationRequest("some-org-name1",
                        "ACTIVE",
                        "test",
                        "sra-id1",
                        "true",
                        "comNum",
                        "company-url1",
                        aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email("someone@somewhere.com")
                                .build(),
                        paymentAccounts,
                        Collections
                                .singletonList(aContactInformationCreationRequest()
                                        .addressLine1("addressLine1")
                                        .addressLine2("addressLine2")
                                        .addressLine3("addressLine3")
                                        .country("country")
                                        .county("county")
                                        .townCity("town-city")
                                        .uprn("uprn")
                                        .postCode("some-post-code")
                                        .dxAddress(Collections
                                                .singletonList(dxAddressCreationRequest()
                                                        .dxNumber("DX 1234567890")
                                                        .dxExchange("dxExchange").build()))
                                        .build()),
                        "Doctor1",
                        orgAttributeRequests);

        return organisationOtherOrgsCreationRequest;

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

    public static List<ContactInformationCreationRequest> getContactInformationList() {
        List<ContactInformationCreationRequest> contactInformationCreationRequests = new ArrayList<>();
        contactInformationCreationRequests.add(
                aContactInformationCreationRequest()
                        .uprn("uprn1")
                        .addressLine1("addressLine1")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country")
                        .county("county")
                        .townCity("town-city")
                        .uprn("uprn")
                        .postCode("some-post-code")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567891")
                                .dxExchange("dxExchange").build()))
                        .build()
        );
        contactInformationCreationRequests.add(
                aContactInformationCreationRequest()
                        .uprn("uprn2")
                        .addressLine1("addressLine1")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country")
                        .county("county")
                        .townCity("town-city")
                        .uprn("uprn")
                        .postCode("some-post-code")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567892")
                                .dxExchange("dxExchange").build()))
                        .build()
        );

        return contactInformationCreationRequests;

    }

    public static List<ContactInformationCreationRequest> createContactInformationCreationRequests() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA" + RandomStringUtils.randomAlphabetic(7));
        paymentAccounts.add("PBA" + RandomStringUtils.randomAlphabetic(7));
        paymentAccounts.add("PBA" + RandomStringUtils.randomAlphabetic(7));

        List<DxAddressCreationRequest> dx1 = new LinkedList<>();
        dx1.add(dxAddressCreationRequest()
                .dxNumber("DX 1234567890")
                .dxExchange("dxExchange").build());
        dx1.add(dxAddressCreationRequest()
                .dxNumber("DX 123456777")
                .dxExchange("dxExchange").build());
        dx1.add(dxAddressCreationRequest()
                .dxNumber("DX 123456788")
                .dxExchange("dxExchange").build());
        List<DxAddressCreationRequest> dx2 = new LinkedList<>();
        dx2.add(dxAddressCreationRequest()
                .dxNumber("DX 123452222")
                .dxExchange("dxExchange").build());
        dx2.add(dxAddressCreationRequest()
                .dxNumber("DX 123456333")
                .dxExchange("dxExchange").build());

        List<ContactInformationCreationRequest> contactInfoList = new LinkedList<>();
        contactInfoList.add(aContactInformationCreationRequest()
                .uprn("uprn1")
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("some-country")
                .county("some-county")
                .townCity("some-town-city")
                .postCode("some-post-code")
                .dxAddress(dx1)
                .build());
        contactInfoList.add(aContactInformationCreationRequest()
                .uprn("uprn2")
                .addressLine1("addLine1")
                .addressLine2("addLine2")
                .addressLine3("addLine3")
                .country("some-country")
                .county("some-county")
                .townCity("some-town-city")
                .postCode("some-post-code")
                .dxAddress(dx2)
                .build());

        return contactInfoList;
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
