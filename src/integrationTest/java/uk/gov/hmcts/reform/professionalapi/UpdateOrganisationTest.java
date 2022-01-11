package uk.gov.hmcts.reform.professionalapi;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

class UpdateOrganisationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void updates_non_existing_organisation_returns_status_404() {
        updateAndValidateOrganisation("AA11NNF", "ACTIVE", null,404);
    }

    @Test
    void updates_organisation_with_organisation_identifier_null_returns_status_400() {
        updateAndValidateOrganisation(null, "ACTIVE", null, 400);
    }

    @Test
    void updates_organisation_with_invalid_organisation_identifier_returns_status_400() {
        updateAndValidateOrganisation("1234ab12", "ACTIVE", null, 400);
    }

    @Test
    void can_update_organisation_status_from_pending_to_active_should_returns_status_200() {
        updateAndValidateOrganisation(createOrganisationRequest(), "ACTIVE", null, 200);
    }

    @Test
    void can_update_organisation_status_from_active_to_blocked_should_returns_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier,"ACTIVE", null, 200);
        updateAndValidateOrganisation(organisationIdentifier,"BLOCKED", null, 200);
    }

    @Test
    void can_update_organisation_status_from_active_to_deleted_should_returns_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier, "ACTIVE", null, 200);
        updateAndValidateOrganisation(organisationIdentifier, "DELETED", null, 200);
    }

    @Test
    void can_update_organisation_status_from_pending_to_deleted_should_returns_status_200() {
        updateAndValidateOrganisation(createOrganisationRequest(), "DELETED", null, 200);
    }

    @Test
    void can_update_organisation_status_from_pending_to_pending_should_returns_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier, "PENDING", null, 200);
    }

    @Test
    void can_update_organisation_status_from_active_to_active_should_returns_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier, "ACTIVE", null, 200);
        updateAndValidateOrganisation(organisationIdentifier, "ACTIVE", null, 200);
    }

    @Test
    void can_update_organisation_status_from_blocked_to_blocked_should_returns_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier, "BLOCKED", null, 200);
        updateAndValidateOrganisation(organisationIdentifier, "BLOCKED", null, 200);
    }

    @Test
    void can_not_update_organisation_status_from_deleted_to_active_should_returns_status_400() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier,"DELETED", null, 200);
        updateAndValidateOrganisation(organisationIdentifier,"ACTIVE", null, 400);
    }

    @Test
    void can_not_update_organisation_status_from_deleted_to_pending_should_returns_status_400() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier, "DELETED", null, 200);
        updateAndValidateOrganisation(organisationIdentifier, "PENDING", null, 400);
    }

    @Test
    void can_not_update_organisation_status_from_deleted_to_blocked_should_returns_status_400() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier, "DELETED", null, 200);
        updateAndValidateOrganisation(organisationIdentifier, "BLOCKED", null, 400);
    }

    @Test
    void can_not_update_organisation_status_from_deleted_to_deleted_should_returns_status_400() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier,"DELETED", null, 200);
        updateAndValidateOrganisation(organisationIdentifier,"DELETED", null, 400);
    }

    @Test
    void can_not_update_organisation_status_from_active_to_pending_should_returns_status_400() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier,"ACTIVE", null, 200);
        updateAndValidateOrganisation(organisationIdentifier, "PENDING", null, 400);
    }

    @Test
    void can_update_organisation_status_from_pending_to_review_should_returns_status_200() {
        updateAndValidateOrganisation(createOrganisationRequest(), "REVIEW", "Company in review", 200);
    }

    @Test
    void can_update_organisation_status_uppercase_active_should_returns_status_200() {
        updateAndValidateOrganisation(createOrganisationRequest(), "ACTIVE", "Company approved", 200);
    }

    @Test
    void can_update_organisation_status_lowercase_active_should_returns_status_200() {
        updateAndValidateOrganisation(createOrganisationRequest(), "active", "Company approved", 200);
    }

    @Test
    void can_not_update_organisation_status_from_active_to_review_should_returns_status_400() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier,"ACTIVE", null, 200);
        updateAndValidateOrganisation(organisationIdentifier,"REVIEW", null, 400);
    }

    @Test
    void can_not_update_organisation_status_empty_returns_status_400() {
        updateAndValidateOrganisation(createOrganisationRequest(), StringUtils.EMPTY, "empty status", 400);
    }

    @Test
    void can_not_update_organisation_status_null_returns_status_400() {
        updateAndValidateOrganisation(createOrganisationRequest(),null, "null status", 400);
    }

    @Test
    void can_not_update_organisation_status_empty_space_returns_status_400() {
        updateAndValidateOrganisation(createOrganisationRequest()," ", "empty space status", 400);
    }

    @Test
    void can_not_update_organisation_status_special_char_returns_status_400() {
        updateAndValidateOrganisation(createOrganisationRequest(),"@status*", "special character status", 400);
    }

    @Test
    void can_not_update_organisation_without_status_returns_status_200() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .statusMessage("statusMessage")
                .build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin,
                        organisationIdentifier);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(200);
    }


    @Test
    void entities_other_than_organisation_should_remain_unchanged_if_updated_and_should_returns_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        Organisation persistedOrganisation = updateAndValidateOrganisation(organisationIdentifier, "ACTIVE", null,
                200);

        List<PaymentAccount> pbaAccounts = persistedOrganisation.getPaymentAccounts();
        PaymentAccount paymentAccount = pbaAccounts.get(0);
        assertThat(paymentAccount.getPbaNumber()).isEqualTo("PBA1234567");
        assertThat(paymentAccount.getPbaStatus()).isEqualTo(ACCEPTED);
        assertThat(paymentAccount.getStatusMessage()).isEqualTo("Auto approved by Admin");

        List<SuperUser> professionalUsers = persistedOrganisation.getUsers();
        SuperUser professionalUser = professionalUsers.get(0);
        assertThat(professionalUser.getFirstName()).isEqualTo("some-fname");
        assertThat(professionalUser.getLastName()).isEqualTo("some-lname");
        assertThat(professionalUser.getEmailAddress()).isEqualTo("someone@somewhere.com");

        List<ContactInformation> contactInformations = persistedOrganisation.getContactInformations();
        ContactInformation contactInformation = contactInformations.get(0);
        assertThat(contactInformation.getUprn()).isEqualTo("uprn");
        assertThat(contactInformation.getAddressLine1()).isEqualTo("addressLine1");
        assertThat(contactInformation.getAddressLine2()).isEqualTo("addressLine2");
        assertThat(contactInformation.getAddressLine3()).isEqualTo("addressLine3");
        assertThat(contactInformation.getTownCity()).isEqualTo("town-city");
        assertThat(contactInformation.getCounty()).isEqualTo("county");
        assertThat(contactInformation.getCountry()).isEqualTo("country");
        assertThat(contactInformation.getPostCode()).isEqualTo("some-post-code");
    }

    @Test
    void fields_other_than_organisation_should_override_if_same_and_should_returns_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateAndValidateOrganisation(organisationIdentifier, "ACTIVE", null,
                200);
    }

    @Test
    void can_not_update_entities_other_than_organisation_should_returns_status_200() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("ACTIVE")
                .name("some-org-name")
                .sraId("sra-id")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber(randomAlphabetic(8))
                .build();
        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin,
                        organisationIdentifier);

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(organisationIdentifier);

        assertThat(persistedOrganisation.getName()).isEqualTo("some-org-name");
        assertThat(persistedOrganisation.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);
        assertThat(persistedOrganisation.getSraId()).isEqualTo("sra-id");
        assertThat(persistedOrganisation.getSraRegulated()).isEqualTo(Boolean.FALSE);
        assertThat(persistedOrganisation.getCompanyUrl()).isEqualTo("company-url");
        assertThat(persistedOrganisation.getCompanyNumber()).isNotNull();
    }

    public String createOrganisationRequest() {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }

    public Organisation updateAndValidateOrganisation(String organisationIdentifier, String status,
                                                      String statusMessage, Integer httpStatus) {
        userProfileCreateUserWireMock(HttpStatus.resolve(httpStatus));
        Organisation persistedOrganisation = null;
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status(status)
                .statusMessage(statusMessage)
                .build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin,
                        organisationIdentifier);

        persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(organisationIdentifier);

        if (httpStatus == 200) {
            assertThat(persistedOrganisation.getName()).isEqualTo("some-org-name1");
            assertThat(persistedOrganisation.getStatus()).isEqualTo(OrganisationStatus.valueOf(status.toUpperCase()));
            if (nonNull(statusMessage)) {
                assertThat(persistedOrganisation.getStatusMessage()).isEqualTo(statusMessage);
            }
            assertThat(persistedOrganisation.getSraId()).isEqualTo("sra-id1");
            assertThat(persistedOrganisation.getSraRegulated()).isEqualTo(Boolean.TRUE);
            assertThat(persistedOrganisation.getCompanyUrl()).isEqualTo("company-url1");
            assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(httpStatus);
            if (OrganisationStatus.ACTIVE.toString() == status) {
                SuperUser professionalUser = persistedOrganisation.getUsers().get(0);
                assertThat(professionalUser.getUserIdentifier()).isNotNull();
                assertThat(persistedOrganisation.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);
            }
        } else {
            if (responseForOrganisationUpdate.get("http_status") instanceof String) {
                assertThat(responseForOrganisationUpdate.get("http_status"))
                        .isEqualTo(String.valueOf(httpStatus.intValue()));
            } else {
                assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(httpStatus);
            }

            if (persistedOrganisation != null && !(OrganisationStatus.PENDING.toString().equals(status)
                                                    || OrganisationStatus.REVIEW.toString().equals(status))) {
                SuperUser professionalUser = persistedOrganisation.getUsers().get(0);
                assertThat(professionalUser.getUserIdentifier()).isNull();
                assertThat(persistedOrganisation.getStatus()).isNotEqualTo(OrganisationStatus.ACTIVE);
            }
        }
        return persistedOrganisation;
    }
}