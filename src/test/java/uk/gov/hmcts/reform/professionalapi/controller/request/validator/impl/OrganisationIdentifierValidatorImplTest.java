package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.TestConstants;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;

public class OrganisationIdentifierValidatorImplTest {

    private Organisation organisation;
    private PaymentAccount paymentAccount;
    private final OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl
            = new OrganisationIdentifierValidatorImpl(mock(OrganisationService.class));

    @Before
    public void setUp() {
        organisation = new Organisation("Company", OrganisationStatus.PENDING, "SraId",
                "12345678", false, "www.company.com");
        organisation.setOrganisationIdentifier(UUID.randomUUID().toString());
        paymentAccount = new PaymentAccount("PBA1234567");
    }

    @Test
    public void test_Validate() {
        Organisation dummyOrganisation = new Organisation("dummyName", OrganisationStatus.ACTIVE, "sraId",
                "12345678", Boolean.FALSE, "dummySite.com");

        organisationIdentifierValidatorImpl.validate(dummyOrganisation, dummyOrganisation.getStatus(),
                dummyOrganisation.getOrganisationIdentifier());
        organisationIdentifierValidatorImpl.validate(dummyOrganisation, OrganisationStatus.ACTIVE,
                dummyOrganisation.getOrganisationIdentifier());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_shouldCheckOrganisationDoesNotExist() {
        organisationIdentifierValidatorImpl.validate(null, null,
                null);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_shouldThrowAEmptyResultDataAccessExceptionWhenPaymentAccountsIsEmpty() {
        organisation.setPaymentAccounts(new ArrayList<>());

        organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(organisation, UUID.randomUUID().toString());
    }

    @Test(expected = AccessDeniedException.class)
    public void test_shouldThrowAAccessDeniedExceptionWhenPaymentAccountsIsEmpty() {
        organisation.setPaymentAccounts(singletonList(paymentAccount));

        organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(organisation, UUID.randomUUID().toString());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_shouldThrowEmptyResultDataAccessExceptionWhenPaymentAccountsIsEmpty() {
        organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(organisation, UUID.randomUUID().toString());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldThrowEmptyResultDataAccessExceptionWhenOrganisationIsNull() {
        organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(null, UUID.randomUUID().toString());
    }

    @Test(expected = Test.None.class)
    public void test_shouldTNothrowExceptionWhenOrganisationIsNotNull() {
        PaymentAccount pba = new PaymentAccount("PBA1234567");
        List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(pba);

        organisation.setPaymentAccounts(paymentAccounts);
        organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(organisation,
                organisation.getOrganisationIdentifier());
    }

    @Test
    public void test_ifUserRoleExistsReturnsTrueForExistingRole() {

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_FINANCE_MANAGER);
        Boolean result = organisationIdentifierValidatorImpl.ifUserRoleExists(authorities,
                TestConstants.PUI_FINANCE_MANAGER);
        assertThat(result).isTrue();
    }

    @Test
    public void test_ifUserRoleExistsReturnsFalseForNonExistingRole() {

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_FINANCE_MANAGER);
        Boolean result = organisationIdentifierValidatorImpl.ifUserRoleExists(authorities, "this-is-a-fake-role");
        assertThat(result).isFalse();
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void test_validateOrganisationIsActiveThrows404WhenOrganisationIsNotActive() {
        organisationIdentifierValidatorImpl.validateOrganisationIsActive(organisation, NOT_FOUND);
    }

    @Test(expected = InvalidRequest.class)
    public void test_validateOrganisationIsActiveThrows400WhenOrganisationIsNotActive() {
        organisationIdentifierValidatorImpl.validateOrganisationIsActive(organisation, BAD_REQUEST);
    }

    @Test(expected = Test.None.class)
    public void test_validateOrganisationIsActiveDoesNotThrow404WhenOrganisationIsActive() {
        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisationIdentifierValidatorImpl.validateOrganisationIsActive(organisation, NOT_FOUND);
    }
}