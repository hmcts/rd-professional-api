package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

public class OrganisationIdentifierValidatorImplTest {

    private Organisation organisationMock = mock(Organisation.class);
    private PaymentAccount paymentAccountMock = mock(PaymentAccount.class);

    OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl = new OrganisationIdentifierValidatorImpl();

    @Test
    public void testValidate() {
        Organisation dummyOrganisation = new Organisation(
                "dummyName",
                OrganisationStatus.ACTIVE,
                "sraId",
                "12345678",
                Boolean.FALSE,
                "dummySite.com");

        organisationIdentifierValidatorImpl.validate(dummyOrganisation, dummyOrganisation.getStatus(), dummyOrganisation.getOrganisationIdentifier());
        try {
            organisationIdentifierValidatorImpl.validate(dummyOrganisation, OrganisationStatus.ACTIVE, dummyOrganisation.getOrganisationIdentifier());
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldCheckOrganisationDoesNotExist() {
        organisationIdentifierValidatorImpl.validate(null, null, null);
    }

    @Test(expected = AccessDeniedException.class)
    public void shouldThrowAccessDeniedExceptionWhenPaymentAccountsIsEmpty() {
        List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(paymentAccountMock);

        when(organisationMock.getOrganisationIdentifier()).thenReturn("huinkj");
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(organisationMock, UUID.randomUUID().toString());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldThrowEmptyResultDataAccessExceptionWhenPaymentAccountsIsEmpty() {
        organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(organisationMock, UUID.randomUUID().toString());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldThrowEmptyResultDataAccessExceptionWhenOrganisationIsNull() {
        organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(null, UUID.randomUUID().toString());
    }
}