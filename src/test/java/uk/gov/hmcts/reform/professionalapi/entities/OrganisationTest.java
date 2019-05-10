package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

public class OrganisationTest {

    private Organisation organisation;

    @Before
    public void setUp() {

        organisation = new Organisation();
        organisation.setName("some-name");
        organisation.setStatus("some-status");
        organisation.setSraId("sra-id");
        organisation.setCompanyNumber("company-number");
        organisation.setSraRegulated(Boolean.FALSE);
        organisation.setCompanyUrl("company-url");
    }

    @Test
    public void creates_organisation_correctly() {

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

        organisation.addProfessionalUser(professionalUser);

        assertThat(organisation.getUsers())
                .containsExactly(professionalUser);
    }

    @Test
    public void adds_payment_account_correctly() {

        PaymentAccount paymentAccount = mock(PaymentAccount.class);

        organisation.addPaymentAccount(paymentAccount);

        assertThat(organisation.getPaymentAccounts())
                .containsExactly(paymentAccount);
    }
}