package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

@Slf4j
public class RetrieveOrganisationsTest extends Service2ServiceEnabledIntegrationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void persists_and_returns_organisation_details() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id1")
                .sraRegulated(Boolean.FALSE)
                .companyUrl("company-url")
                .companyNumber("company")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .pbaAccounts(Arrays.asList(aPbaPaymentAccount()
                .pbaNumber("pbaNumber")
                .build()))
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country")
                        .county("county")
                        .townCity("town-city")
                        .postCode("post-code-test")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();

        Map<String, Object> response =
              professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifierResponse);

        assertThat(orgResponse.get("http_status").toString().contains("OK"));
        assertThat(orgResponse.get("organisationIdentifier")).isEqualTo(orgIdentifierResponse);

        assertThat(orgResponse.get("name")).isEqualTo("some-org-name");
        assertThat(orgResponse.get("sraId")).isEqualTo("sra-id1");
        assertThat(orgResponse.get("sraRegulated")).isEqualTo(false);
        assertThat(orgResponse.get("companyUrl")).isEqualTo("company-url");
        assertThat(orgResponse.get("companyNumber")).isEqualTo("company");

        Map<String, Object> superUser = ((List<Map<String, Object>>) orgResponse.get("superUser")).get(0);
        assertThat(superUser.get("firstName")).isEqualTo("some-fname");
        assertThat(superUser.get("lastName")).isEqualTo("some-lname");
        assertThat(superUser.get("email")).isEqualTo("someone@somewhere.com");

        List<Map<String, Object>> accounts = (List<Map<String, Object>>) orgResponse.get("pbaAccounts");
        assertThat(accounts.get(0).get("pbaNumber")).isEqualTo("pbaNumber");

        Map<String, Object> contactInfo = ((List<Map<String, Object>>) orgResponse.get("contactInformation")).get(0);
        assertThat(contactInfo.get("addressLine1")).isEqualTo("addressLine1");
        assertThat(contactInfo.get("addressLine2")).isEqualTo("addressLine2");
        assertThat(contactInfo.get("addressLine3")).isEqualTo("addressLine3");
        assertThat(contactInfo.get("county")).isEqualTo("county");
        assertThat(contactInfo.get("country")).isEqualTo("country");
        assertThat(contactInfo.get("townCity")).isEqualTo("town-city");
        assertThat(contactInfo.get("postCode")).isEqualTo("post-code-test");

        Map<String, Object> dxAddress = ((List<Map<String, Object>>) contactInfo.get("dxAddress")).get(0);
        assertThat(dxAddress.get("dxNumber")).isEqualTo("DX 1234567890");
        assertThat(dxAddress.get("dxExchange")).isEqualTo("dxExchange");

        log.info("RetrieveOrganisationsTest:Received response to retrieve an organisation details...");
    }

    @Test
    public void persists_and_returns_all_organisations() {
        professionalReferenceDataClient.createOrganisation(anOrganisationCreationRequest()
                .name("some-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .build());
        professionalReferenceDataClient.createOrganisation(anOrganisationCreationRequest()
                .name("some-other-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someoneElse@somewhere.com")
                        .build())
                .build());

        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisations();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
        assertThat(((List<?>) orgResponse.get("organisations")).size()).isEqualTo(2);
    }
}
