package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.TestConstants;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class OrganisationIdentifierValidatorImplTest {

    private Organisation organisation;
    private PaymentAccount paymentAccount;
    private final OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl
            = new OrganisationIdentifierValidatorImpl(mock(OrganisationService.class));

    @BeforeEach
    void setUp() {
        organisation = new Organisation("Company", OrganisationStatus.PENDING, "SraId",
                "12345678", false, "www.company.com");
        organisation.setOrganisationIdentifier(UUID.randomUUID().toString());
        paymentAccount = new PaymentAccount("PBA1234567");
    }

    @Test
    void test_Validate() {
        Organisation dummyOrganisation = new Organisation("dummyName", OrganisationStatus.ACTIVE, "sraId",
                "12345678", Boolean.FALSE, "dummySite.com");

        organisationIdentifierValidatorImpl.validate(dummyOrganisation, dummyOrganisation.getStatus(),
                dummyOrganisation.getOrganisationIdentifier());
        organisationIdentifierValidatorImpl.validate(dummyOrganisation, OrganisationStatus.ACTIVE,
                dummyOrganisation.getOrganisationIdentifier());
    }

    @Test
    void test_shouldCheckOrganisationDoesNotExist() {
        assertThrows(EmptyResultDataAccessException.class, () ->
                organisationIdentifierValidatorImpl.validate(null, null,
                        null));
    }

    @Test
    void test_shouldThrowAEmptyResultDataAccessExceptionWhenPaymentAccountsIsEmpty() {
        organisation.setPaymentAccounts(new ArrayList<>());

        String uuid = UUID.randomUUID().toString();

        assertThrows(EmptyResultDataAccessException.class, () ->
                organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(
                        organisation, uuid));
    }

    @Test
    void test_shouldThrowAAccessDeniedExceptionWhenPaymentAccountsIsEmpty() {
        organisation.setPaymentAccounts(singletonList(paymentAccount));

        String uuid = UUID.randomUUID().toString();

        assertThrows(AccessDeniedException.class, () ->
                organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(
                        organisation, uuid));
    }

    @Test
    void test_shouldThrowEmptyResultDataAccessExceptionWhenPaymentAccountsIsEmpty() {
        String uuid = UUID.randomUUID().toString();

        assertThrows(EmptyResultDataAccessException.class, () ->
                organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(
                        organisation, uuid));
    }

    @Test
    void shouldThrowEmptyResultDataAccessExceptionWhenOrganisationIsNull() {
        String uuid = UUID.randomUUID().toString();

        assertThrows(EmptyResultDataAccessException.class, () ->
                organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(
                        null, uuid));
    }

    @Test
    void test_shouldTNothrowExceptionWhenOrganisationIsNotNull() {
        PaymentAccount pba = new PaymentAccount("PBA1234567");
        List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(pba);

        organisation.setPaymentAccounts(paymentAccounts);
        assertDoesNotThrow(() ->
                organisationIdentifierValidatorImpl.verifyExtUserOrgIdentifier(organisation,
                        organisation.getOrganisationIdentifier()));
    }

    @Test
    void test_ifUserRoleExistsReturnsTrueForExistingRole() {

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_FINANCE_MANAGER);
        Boolean result = organisationIdentifierValidatorImpl.ifUserRoleExists(authorities,
                TestConstants.PUI_FINANCE_MANAGER);
        assertThat(result).isTrue();
    }

    @Test
    void test_ifUserRoleExistsReturnsFalseForNonExistingRole() {

        List<String> authorities = new ArrayList<>();
        authorities.add(TestConstants.PUI_FINANCE_MANAGER);
        Boolean result = organisationIdentifierValidatorImpl.ifUserRoleExists(authorities, "this-is-a-fake-role");
        assertThat(result).isFalse();
    }

    @Test
    void test_validateOrganisationIsActiveThrows400WhenOrganisationIsNotActive() {
        assertThrows(InvalidRequest.class,() ->
                organisationIdentifierValidatorImpl.validateOrganisationIsActive(organisation, BAD_REQUEST));
    }

    @Test
    void test_validateOrganisationIsActiveDoesNotThrow404WhenOrganisationIsActive() {
        organisation.setStatus(OrganisationStatus.ACTIVE);
        assertDoesNotThrow(() ->
                organisationIdentifierValidatorImpl.validateOrganisationIsActive(organisation, NOT_FOUND));
    }

    @Test
    void test_validateGetRefreshUsersParamsWhenBothParamsNull() {
        assertThrows(InvalidRequest.class,() ->
                organisationIdentifierValidatorImpl.validateGetRefreshUsersParams(null, null, null, null));
    }

    @Test
    void test_validateGetRefreshUsersParamsWithInvalidSize() {
        UUID uuid = UUID.randomUUID();
        assertThrows(InvalidRequest.class,() -> organisationIdentifierValidatorImpl
                .validateGetRefreshUsersParams("2023-12-05T14:49:53", null, -1, uuid));
    }

    @Test
    void test_validateGetRefreshUsersParamsWithBadDateTimeFormat() {
        assertThrows(InvalidRequest.class,() -> organisationIdentifierValidatorImpl
                .validateGetRefreshUsersParams("Bad date time pattern", null, null, null));
    }

    @Test
    void test_validateGetRefreshUsersParamsWithValidFormat() {
        assertDoesNotThrow(() -> organisationIdentifierValidatorImpl
                .validateGetRefreshUsersParams("2023-12-05T14:49:53", null, 1, null));
    }

    @Test
    void test_validateSince() {
        assertThrows(InvalidRequest.class,() -> organisationIdentifierValidatorImpl.validateSince("bad format"));
        organisationIdentifierValidatorImpl.validateSince(null);
        organisationIdentifierValidatorImpl.validateSince("2023-12-05T14:49:53");
    }

}
