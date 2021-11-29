package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.BAD_REQUEST_STR;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import java.util.List;

public class OrganisationStatusValidatorImplTest {

    private OrganisationStatusValidatorImpl organisationStatusValidatorImpl;
    private Organisation organisation;
    private String orgId;

    @Before
    public void setUp() {
        organisationStatusValidatorImpl = new OrganisationStatusValidatorImpl();
        organisation = new Organisation("dummyName", OrganisationStatus.ACTIVE, "sraId",
                "12345678", Boolean.FALSE, "dummySite.com");
        orgId = organisation.getOrganisationIdentifier();
    }

    @Test
    public void test_Validate() {
        try {
            organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.ACTIVE, orgId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test(expected = InvalidRequest.class)
    public void test_ThrowsExceptionWhenCurrentStatusActiveAndInputStatusPending() {
        organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.PENDING, orgId);
    }

    @Test(expected = InvalidRequest.class)
    public void test_ThrowsExceptionWhenCurrentStatusDeleted() {
        organisation.setStatus(OrganisationStatus.DELETED);
        organisationStatusValidatorImpl.validate(organisation, OrganisationStatus.ACTIVE, orgId);
    }

    @Test
    public void test_validateAndReturnStatusList_allvalid() {
        List<String> validStatuses = OrganisationStatusValidatorImpl.validateAndReturnStatusList(
                "ACTIVE,REVIEW");
        assertThat(validStatuses).hasSize(2).contains("ACTIVE","REVIEW");

        validStatuses = OrganisationStatusValidatorImpl.validateAndReturnStatusList(
                "  , ACTIVE,REVIEW,");
        assertThat(validStatuses).hasSize(2).contains("ACTIVE","REVIEW");

        validStatuses = OrganisationStatusValidatorImpl.validateAndReturnStatusList(
                " ACTIVE , REVIEW ");
        assertThat(validStatuses).hasSize(2).contains("ACTIVE","REVIEW");

        validStatuses = OrganisationStatusValidatorImpl.validateAndReturnStatusList(
                " AcTiVe , rEvIeW ");
        assertThat(validStatuses).hasSize(2).contains("ACTIVE","REVIEW");
    }

    @Test
    public void test_validateAndReturnStatusList_null_empty_check() {
        String errorMessage = BAD_REQUEST_STR + "Invalid status(es) passed : " + "null or empty";
        verifyException(null, errorMessage);
        verifyException("", errorMessage);
        verifyException(" ", errorMessage);
    }

    @Test
    public void test_validateAndReturnStatusList_invalid_status_check() {
        String errorMessage = BAD_REQUEST_STR + "Invalid status(es) passed : ";
        verifyException("ACTIV,REVIEW ", errorMessage + "ACTIV,REVIEW");
        verifyException("ACTIVE,,REVIEW", errorMessage + "ACTIVE,,REVIEW");
        verifyException("null", errorMessage + "null");
    }

    public void verifyException(String status, String message) {
        Assertions.assertThatThrownBy(() -> OrganisationStatusValidatorImpl.validateAndReturnStatusList(status))
                .isExactlyInstanceOf(InvalidRequest.class)
                .hasMessage(message);
    }
}