package uk.gov.hmcts.reform.professionalapi.utils;

import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build()));
    }

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder whiteSpaceTrimOrganisationRequest() {
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA1234567");

        return anOrganisationCreationRequest()
                .name("  some-  org -name  ")
                .superUser(aUserCreationRequest()
                        .firstName(" some-fname    b    ")
                        .lastName(" some-         lname  ")
                        .email(" someone@s omewh ere.com ")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build()));


    }

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder organisationRequestWithAllFields() {
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA1234567");

        return anOrganisationCreationRequest()
            .name("some-org-name")
            .status(OrganisationStatus.PENDING)
            .sraId("sra-id")
            .sraRegulated(Boolean.FALSE)
            .companyUrl("company -url")
            .companyNumber("company")
            .paymentAccount(paymentAccounts)
            .superUser(aUserCreationRequest()
                .firstName("some-fname")
                .lastName("some-lname")
                .email("someone@somewhere.com")
                .jurisdictions(createJurisdictions())
                .build())
            .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("country")
                .county("county")
                .townCity("town-city")
                .postCode("some-post-code")
                    .dxAddress(Arrays.asList(dxAddressCreationRequest()
                        .dxNumber("DX 1234567890")
                        .dxExchange("dxExchange").build()))
                .build()));
    }

    public static OrganisationCreationRequest.OrganisationCreationRequestBuilder organisationRequestWithAllFieldsAreUpdated() {
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA1234567");

        return anOrganisationCreationRequest()
            .name("some-org-name1")
            .status(OrganisationStatus.ACTIVE)
            .sraId("sra-id1")
            .sraRegulated(Boolean.TRUE)
            .companyUrl("company-url1")
            .companyNumber("company1")
            .paymentAccount(paymentAccounts)
            .superUser(aUserCreationRequest()
                .firstName("somefname")
                .lastName("somelname")
                .email("someone1@somewhere.com")
                .jurisdictions(createJurisdictions())
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

    public static List<Map<String,String>> createJurisdictions() {

        List<Map<String,String>> jurisdictions = new ArrayList<>();
        Map<String,String> jurisdictionId1 = new HashMap<>();
        jurisdictionId1.put("id", "Probate");
        Map<String,String> jurisdictionId2 = new HashMap<>();
        jurisdictionId2.put("id", "Bulk Scanning");

        jurisdictions.add(jurisdictionId1);
        jurisdictions.add(jurisdictionId2);
        return jurisdictions;
    }
}
