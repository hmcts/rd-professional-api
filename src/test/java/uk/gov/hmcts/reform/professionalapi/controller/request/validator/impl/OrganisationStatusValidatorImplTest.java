package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.BAD_REQUEST_STR;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.REVIEW;

@ExtendWith(MockitoExtension.class)
class OrganisationStatusValidatorImplTest {

    private OrganisationStatusValidatorImpl organisationStatusValidatorImpl;
    private Organisation organisation;
    private String orgId;

    @BeforeEach
    void setUp() {
        organisationStatusValidatorImpl = new OrganisationStatusValidatorImpl();
        organisation = new Organisation("dummyName", OrganisationStatus.ACTIVE, "sraId",
                "12345678", Boolean.FALSE, null,"dummySite.com");
        orgId = organisation.getOrganisationIdentifier();
    }

    @Test
    void test_Validate() {
        try {
            organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.ACTIVE, orgId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void test_ThrowsExceptionWhenCurrentStatusActiveAndInputStatusPending() {
        assertThrows(InvalidRequest.class, () ->
                organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.PENDING, orgId));
    }

    @Test
    void test_ThrowsExceptionWhenCurrentStatusDeleted() {
        organisation.setStatus(OrganisationStatus.DELETED);
        assertThrows(InvalidRequest.class, () ->
                organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.ACTIVE, orgId));
    }

    @Test
    void test_validateAndReturnStatusList_allvalid() {
        List<OrganisationStatus> validStatuses = organisationStatusValidatorImpl.validateAndReturnStatusList(
                "ACTIVE,REVIEW");
        assertThat(validStatuses).hasSize(2).contains(ACTIVE,REVIEW);

        validStatuses = organisationStatusValidatorImpl.validateAndReturnStatusList(
                "  , ACTIVE,REVIEW,");
        assertThat(validStatuses).hasSize(2).contains(ACTIVE,REVIEW);

        validStatuses = organisationStatusValidatorImpl.validateAndReturnStatusList(
                " ACTIVE , REVIEW ");
        assertThat(validStatuses).hasSize(2).contains(ACTIVE,REVIEW);

        validStatuses = organisationStatusValidatorImpl.validateAndReturnStatusList(
                " AcTiVe , rEvIeW ");
        assertThat(validStatuses).hasSize(2).contains(ACTIVE,REVIEW);
    }

    @Test
    void test_validateAndReturnStatusList_null_empty_check() {
        String errorMessage = BAD_REQUEST_STR + "Invalid status(es) passed : " + "null or empty";
        verifyException(null, errorMessage);
        verifyException("", errorMessage);
        verifyException(" ", errorMessage);
    }

    @Test
    void test_validateAndReturnStatusList_invalid_status_check() {
        String errorMessage = BAD_REQUEST_STR + "Invalid status(es) passed : ";
        verifyException("ACTIV,REVIEW ", errorMessage + "ACTIV,REVIEW");
        verifyException("ACTIVE,,REVIEW", errorMessage + "ACTIVE,,REVIEW");
        verifyException("null", errorMessage + "null");
    }

    void verifyException(String status, String message) {
        assertThatThrownBy(() -> organisationStatusValidatorImpl.validateAndReturnStatusList(status))
                .isExactlyInstanceOf(InvalidRequest.class)
                .hasMessage(message);
    }
}