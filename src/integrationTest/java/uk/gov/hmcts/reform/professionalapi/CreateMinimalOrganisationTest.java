package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.whiteSpaceTrimOrganisationRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


public class CreateMinimalOrganisationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void persists_and_returns_valid_minimal_organisation() {

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        assertThat(orgIdentifierResponse).isNotNull();
        assertThat(orgIdentifierResponse.length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
        assertThat(orgIdentifierResponse.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)).isTrue();

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(orgIdentifierResponse);

        ProfessionalUser persistedSuperUser = persistedOrganisation.getUsers().get(0).toProfessionalUser();

        assertThat(persistedOrganisation.getOrganisationIdentifier()).isNotNull();
        assertThat(persistedOrganisation.getOrganisationIdentifier()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(1);

        assertThat(persistedSuperUser.getEmailAddress()).isEqualTo("someone@somewhere.com");
        assertThat(persistedSuperUser.getFirstName()).isEqualTo("some-fname");
        assertThat(persistedSuperUser.getLastName()).isEqualTo("some-lname");
        assertThat(persistedSuperUser.getOrganisation().getName()).isEqualTo("some-org-name");
        assertThat(persistedSuperUser.getOrganisation().getId()).isEqualTo(persistedOrganisation.getId());
        assertThat(persistedOrganisation.getName()).isEqualTo("some-org-name");

        PrdEnumId prdEnumId1 = new PrdEnumId(10,"JURISD_ID");
        PrdEnumId prdEnumId2 = new PrdEnumId(13,"JURISD_ID");
        PrdEnum prdEnum1 = new PrdEnum(prdEnumId1, "PROBATE", "PROBATE");
        PrdEnum prdEnum2 = new PrdEnum(prdEnumId2, "BULKSCAN", "BULKSCAN");
        UserAttribute jurisAttribute1 = new UserAttribute(persistedSuperUser, prdEnum1);
        UserAttribute jurisAttribute2 = new UserAttribute(persistedSuperUser, prdEnum1);
        List<ProfessionalUser> professionalUser = professionalUserRepository.findByOrganisation(persistedOrganisation);
        assertThat(professionalUser.get(0).getUserAttributes().get(4).getPrdEnum().getEnumName()).isEqualTo("organisation-admin");
        assertThat(professionalUser.get(0).getUserAttributes().contains(jurisAttribute1));
        assertThat(professionalUser.get(0).getUserAttributes().contains(jurisAttribute2));


    }

    @Test
    public void returns_400_when_mandatory_data_not_present() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name(null)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString().contains("Bad Request"));

        assertThat(organisationRepository.findAll()).isEmpty();
    }

    @Test
    public void returns_400_when_email_not_valid() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("somename")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("@@someone@somewhere.com")
                        .build())
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString().contains("Bad Request"));

        assertThat(organisationRepository.findAll()).isEmpty();
    }

    @Test
    public void returns_400_when_email_minus_not_valid() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("-someone@somewhere.com")
                        .build())
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString().contains("Bad Request"));

        assertThat(organisationRepository.findAll()).isEmpty();
    }

    @Test
    public void returns_200_when_email_has_underscore() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some_one_gh@somewhere.com")
                        .jurisdictions(OrganisationFixtures.createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build())).build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("201 CREATED");

    }

    @Test
    public void returns_500_when_database_constraint_violated() {
        String organisationNameViolatingDatabaseMaxLengthConstraint = RandomStringUtils.random(256);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest()
                .name(organisationNameViolatingDatabaseMaxLengthConstraint).build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("500");
    }


    @Test
    public void whiteSpaceRemovalTest() {
        OrganisationCreationRequest organisationCreationRequest = whiteSpaceTrimOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        assertThat(orgIdentifierResponse).isNotNull();
        assertThat(orgIdentifierResponse.length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
        assertThat(orgIdentifierResponse.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)).isTrue();

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(orgIdentifierResponse);

        SuperUser persistedSuperUser = persistedOrganisation.getUsers().get(0);
        ProfessionalUser professionalUser = professionalUserRepository.findByUserIdentifier(persistedSuperUser.getUserIdentifier());

        assertThat(persistedOrganisation.getOrganisationIdentifier()).isNotNull();
        assertThat(persistedOrganisation.getOrganisationIdentifier()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(1);

        assertThat(persistedSuperUser.getEmailAddress()).isEqualTo("someone@somewhere.com");
        assertThat(persistedSuperUser.getFirstName()).isEqualTo("some-fname b");
        assertThat(persistedSuperUser.getLastName()).isEqualTo("some- lname");
        assertThat(persistedSuperUser.getOrganisation().getName()).isEqualTo("some- org -name");
        assertThat(persistedSuperUser.getOrganisation().getId()).isEqualTo(persistedOrganisation.getId());
        assertThat(professionalUser.getUserAttributes().get(4).getPrdEnum().getEnumName()).isEqualTo("organisation-admin");
        assertThat(persistedOrganisation.getName()).isEqualTo("some- org -name");

    }

    @Test
    public void returns_400_when_sraid_is_duplicate() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(OrganisationFixtures.createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build())).build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        Map<String, Object> response1 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response1.get("http_status")).isEqualTo("400");
        assertThat(response1.get("response_body").toString().contains("attempt to insert or update data resulted in violation of an integrity constraint for field SRA_ID"));
    }

    @Test
    public void persists_and_returns_400_company_number_is_not_unique() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber("same1010")
                .superUser(aUserCreationRequest()
                        .firstName(" some-fname ")
                        .lastName(" some-lname ")
                        .email("someone@somewhere.com")
                        .jurisdictions(OrganisationFixtures.createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("201 CREATED");

        Map<String, Object> response2 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response2.get("http_status")).isEqualTo("400");
        assertThat(response2.get("response_body").toString().contains("attempt to insert or update data resulted in violation of an integrity constraint for field COMPANY_NUMBER"));
    }

    @Test
    public void returns_200_when_company_number_length_less_than_8() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some")
                .companyNumber("123456")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some_one_gh@somewhere.com")
                        .jurisdictions(OrganisationFixtures.createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build())).build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("201 CREATED");
    }

    @Test
    public void returns_400_when_company_number_length_greater_than_8() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some")
                .companyNumber("123456789")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some_one_gh@somewhere.com")
                        .jurisdictions(OrganisationFixtures.createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build())).build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void returns_200_when_valid_email_is_passed() {

        String[] emails = new String[] {"v.greeny@ashfords.co.uk", "j.johnson@timms-law.com"};


        OrganisationCreationRequest.OrganisationCreationRequestBuilder organisationCreationRequest = someMinimalOrganisationRequest();

        Arrays.stream(emails).forEach(email -> {

            organisationCreationRequest.superUser(aUserCreationRequest().email(email).firstName("fname").lastName("lname").jurisdictions(OrganisationFixtures.createJurisdictions()).build());
            Map<String, Object> response =
                    professionalReferenceDataClient.createOrganisation(organisationCreationRequest.build());
            assertThat(response.get("http_status")).isEqualTo("201 CREATED");
        });



    }
}
