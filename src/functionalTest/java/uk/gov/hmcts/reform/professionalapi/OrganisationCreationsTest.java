package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.Arrays;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class OrganisationCreationsTest extends AuthorizationFunctionalTest {

    @Test
    public void can_create_an_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
    }

    @Test
    public void ac1_can_create_an_organisation_with_valid_Dx_Number_and_valid_Dx_Exchange() {
        OrganisationCreationRequest organisationCreationRequest = createOrganisationWithDxEntity("DX1A3B5C7D9E0", "Th1s 1s v@l1d");
        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
    }


    @Test
    public void ac2_create_an_organisation_with_Dx_Number_longer_than_13_throws_400() {
        OrganisationCreationRequest organisationCreationRequest = createOrganisationWithDxEntity("this is too many characters", "Th1s 1s v@l1d");
        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void ac3_create_an_organisation_with_Dx_Exchange_longer_than_20_throws_400() {
        OrganisationCreationRequest organisationCreationRequest = createOrganisationWithDxEntity("DX1234567890", "this is too many characters");
        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void ac4_create_an_organisation_with_Dx_Number_empty_throws_400() {
        OrganisationCreationRequest organisationCreationRequest = createOrganisationWithDxEntity("", "Th1s 1s v@l1d");
        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void ac5_create_an_organisation_with_Dx_Exchange_empty_throws_400() {
        OrganisationCreationRequest organisationCreationRequest = createOrganisationWithDxEntity("DX1234567890", "");
        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void ac6_create_an_organisation_with_Dx_Number_not_provided_throws_400() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxExchange("Th1s 1s v@l1d").build()))
                        .build()))
                .build();

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void ac7_create_an_organisation_with_Dx_Exchange_not_provided_throws_400() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX1234567890X")
                                .build()))
                        .build()))
                .build();

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    private OrganisationCreationRequest createOrganisationWithDxEntity(String dxNumber, String dxExchange) {
        return anOrganisationCreationRequest()
                .name("some-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber(dxNumber)
                                .dxExchange(dxExchange).build()))
                        .build()))
                .build();
    }
}