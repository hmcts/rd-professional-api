package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class OrganisationTest {

    @Test
    public void creates_organisation_correctly() {

        Organisation organisation = new Organisation("some-name", "some-status",
                "sra-id","company-number",Boolean.FALSE,"company-url");

        assertThat(organisation.getName()).isEqualTo("some-name");
        assertThat(organisation.getStatus()).isEqualTo("some-status");
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

        organisation.setOrganisationIdentifier(UUID.randomUUID());

        assertThat(organisation.getOrganisationIdentifier()).isNotNull();
    }

    @Test
    public void adds_users_correctly() {

        ProfessionalUser professionalUser = mock(ProfessionalUser.class);

        Organisation organisation = new Organisation();
        organisation.addProfessionalUser(professionalUser);

        assertThat(organisation.getUsers())
                .containsExactly(professionalUser);
    }

    @Test
    public void adds_payment_account_correctly() {

        PaymentAccount paymentAccount = mock(PaymentAccount.class);

        Organisation organisation = new Organisation();
        organisation.addPaymentAccount(paymentAccount);

        assertThat(organisation.getPaymentAccounts())
                .containsExactly(paymentAccount);
    }

}