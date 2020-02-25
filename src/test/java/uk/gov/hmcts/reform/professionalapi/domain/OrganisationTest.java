package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class OrganisationTest {

    @Test
    public void creates_organisation_correctly() {
        List<ContactInformation> contactInformations = new ArrayList<>();

        Organisation organisation = new Organisation("some-name", OrganisationStatus.ACTIVE,
                "sra-id", "company-number", Boolean.FALSE, "company-url");
        organisation.setLastUpdated(LocalDateTime.now());
        organisation.setCreated(LocalDateTime.now());
        organisation.setContactInformations(contactInformations);
        organisation.setOrganisationIdentifier(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));

        assertThat(organisation.isOrganisationStatusActive()).isTrue();
        assertThat(organisation.getName()).isEqualTo("some-name");
        assertThat(organisation.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);
        assertThat(organisation.getSraId()).isEqualTo("sra-id");
        assertThat(organisation.getCompanyNumber()).isEqualTo("company-number");
        assertThat(organisation.getSraRegulated()).isEqualTo(Boolean.FALSE);
        assertThat(organisation.getCompanyUrl()).isEqualTo("company-url");
        assertThat(organisation.getId()).isNull(); // hibernate generated
        assertThat(organisation.getLastUpdated()).isNotNull();
        assertThat(organisation.getCreated()).isNotNull();
        assertThat(organisation.getContactInformations()).isNotNull();
        assertThat(organisation.getOrganisationIdentifier()).isNotNull();
        assertThat(organisation.getOrganisationIdentifier().length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
    }

    @Test
    public void adds_users_correctly() {
        SuperUser superUser = new SuperUser();
        Organisation organisation = new Organisation();
        organisation.addProfessionalUser(superUser);

        assertThat(organisation.getUsers()).containsExactly(superUser);
    }

    @Test
    public void adds_payment_account_correctly() {
        PaymentAccount paymentAccount = new PaymentAccount();
        Organisation organisation = new Organisation();
        organisation.addPaymentAccount(paymentAccount);

        assertThat(organisation.getPaymentAccounts()).containsExactly(paymentAccount);
    }

    @Test
    public void adds_contact_information_correctly() {
        ContactInformation contactInformation = new ContactInformation();
        Organisation organisation = new Organisation();
        organisation.addContactInformation(contactInformation);

        assertThat(organisation.getContactInformation()).containsExactly(contactInformation);
    }
}