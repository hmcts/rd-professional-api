package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganisationTest {

    @Test
    void test_creates_organisation_correctly() {
        List<ContactInformation> contactInformations = new ArrayList<>();

        Organisation organisation = new Organisation("some-name", OrganisationStatus.ACTIVE,
                "sra-id", "company-number", Boolean.FALSE, "company-url");
        organisation.setLastUpdated(LocalDateTime.now());
        organisation.setCreated(LocalDateTime.now());
        organisation.setContactInformations(contactInformations);
        organisation.setOrganisationIdentifier(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));
        organisation.setStatusMessage("statusMessage");

        assertThat(organisation.isOrganisationStatusActive()).isTrue();
        assertThat(organisation.getName()).isEqualTo("some-name");
        assertThat(organisation.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);
        assertThat(organisation.getStatusMessage()).isEqualTo("statusMessage");
        assertThat(organisation.getSraId()).isEqualTo("sra-id");
        assertThat(organisation.getCompanyNumber()).isEqualTo("company-number");
        assertThat(organisation.getSraRegulated()).isEqualTo(Boolean.FALSE);
        assertThat(organisation.getCompanyUrl()).isEqualTo("company-url");
        assertThat(organisation.getId()).isNull(); // hibernate generated
        assertThat(organisation.getLastUpdated()).isNotNull();
        assertThat(organisation.getCreated()).isNotNull();
        assertThat(organisation.getContactInformations()).isNotNull();
        assertThat(organisation.getOrganisationIdentifier()).isNotNull();
        assertThat(organisation.getOrganisationIdentifier()).hasSize(LENGTH_OF_ORGANISATION_IDENTIFIER);
    }

    @Test
    void test_adds_users_correctly() {
        SuperUser superUser = new SuperUser();
        Organisation organisation = new Organisation();
        organisation.addProfessionalUser(superUser);

        assertThat(organisation.getUsers()).containsExactly(superUser);
    }

    @Test
    void test_adds_payment_account_correctly() {
        PaymentAccount paymentAccount = new PaymentAccount();
        Organisation organisation = new Organisation();
        organisation.addPaymentAccount(paymentAccount);
        organisation.setId(UUID.randomUUID());
        organisation.setSraRegulated(false);

        assertThat(organisation.getPaymentAccounts()).containsExactly(paymentAccount);
        assertThat(organisation.getId()).isNotNull();
        assertFalse(organisation.getSraRegulated());

        organisation.setSraRegulated(true);
        assertTrue(organisation.getSraRegulated());

    }

    @Test
    void test_adds_contact_information_correctly() {
        ContactInformation contactInformation = new ContactInformation();
        Organisation organisation = new Organisation();
        organisation.addContactInformation(contactInformation);

        assertThat(organisation.getContactInformation()).containsExactly(contactInformation);
        assertThat(organisation.getContactInformations()).isNotEmpty();
    }

    @Test
    void test_adds_organisation_mfa_status_correctly() {
        OrganisationMfaStatus organisationMfaStatus = new OrganisationMfaStatus();
        Organisation organisation = new Organisation();
        organisation.setOrganisationMfaStatus(organisationMfaStatus);

        assertThat(organisation.getOrganisationMfaStatus()).isNotNull();
        assertThat(organisation.getOrganisationMfaStatus()).isEqualTo(organisationMfaStatus);
    }
}