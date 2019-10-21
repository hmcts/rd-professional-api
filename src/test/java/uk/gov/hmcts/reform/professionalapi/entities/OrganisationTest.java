package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

public class OrganisationTest {

    @Test
    public void creates_organisation_correctly() {

        Organisation organisation = new Organisation("some-name", OrganisationStatus.ACTIVE,
                "sra-id","company-number",Boolean.FALSE,"company-url");

        assertThat(organisation.isOrganisationStatusActive()).isTrue();

        assertThat(organisation.getName()).isEqualTo("some-name");
        assertThat(organisation.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);
        assertThat(organisation.getSraId()).isEqualTo("sra-id");
        assertThat(organisation.getCompanyNumber()).isEqualTo("company-number");
        assertThat(organisation.getSraRegulated()).isEqualTo(Boolean.FALSE);
        assertThat(organisation.getCompanyUrl()).isEqualTo("company-url");
        assertThat(organisation.getId()).isNull();              // hibernate generated

        organisation.setLastUpdated(LocalDateTime.now());

        organisation.setCreated(LocalDateTime.now());

        assertThat(organisation.getLastUpdated()).isNotNull();

        assertThat(organisation.getCreated()).isNotNull();

        List<ContactInformation> cis = new ArrayList<>();

        organisation.setContactInformations(cis);

        assertThat(organisation.getContactInformations()).isNotNull();

        organisation.setOrganisationIdentifier(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));

        assertThat(organisation.getOrganisationIdentifier()).isNotNull();

        assertThat(organisation.getOrganisationIdentifier().length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
    }

    @Test
    public void adds_users_correctly() {

        SuperUser superUser = mock(SuperUser.class);

        Organisation organisation = new Organisation();
        organisation.addProfessionalUser(superUser);

        assertThat(organisation.getUsers())
                .containsExactly(superUser);
    }

    @Test
    public void adds_payment_account_correctly() {

        PaymentAccount paymentAccount = mock(PaymentAccount.class);

        Organisation organisation = new Organisation();
        organisation.addPaymentAccount(paymentAccount);

        assertThat(organisation.getPaymentAccounts())
                .containsExactly(paymentAccount);
    }

    @Test
    public void adds_contact_information_correctly() {
        ContactInformation contactInformation = mock(ContactInformation.class);

        Organisation organisation = new Organisation();
        organisation.addContactInformation(contactInformation);

        assertThat(organisation.getContactInformation())
                .containsExactly(contactInformation);
    }

}