package uk.gov.hmcts.reform.professionalapi.controller.request.controller.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.RequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationCreationRequestValidatorTest {

    @Mock
    RequestValidator validator1;

    @Mock
    RequestValidator validator2;

    @Mock
    OrganisationCreationRequest request;

    OrganisationCreationRequestValidator organisationCreationRequestValidator;

    @Before
    public void setup() {
        organisationCreationRequestValidator =
                new OrganisationCreationRequestValidator(asList(validator1, validator2));
    }

    @Test
    public void calls_all_validators() {

        organisationCreationRequestValidator.validate(request);

        verify(validator1, times(1)).validate(request);
        verify(validator2, times(1)).validate(request);

        assertThat(OrganisationCreationRequestValidator.contains(OrganisationStatus.PENDING.name())).isEqualTo(true);
        assertThat(OrganisationCreationRequestValidator.contains("pend")).isEqualTo(false);
    }

    @Test(expected = InvalidRequest.class)
    public void organisation_identifier_null_should_throw_exception() {
        organisationCreationRequestValidator.validateOrganisationIdentifier(null);
    }

    @Test(expected = InvalidRequest.class)
    public void organisation_identifier_with_length_less_than_expected_should_throw_exception() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("1ACVD");
    }

    @Test(expected = InvalidRequest.class)
    public void organisation_identifier_with_length_greater_than_expected_should_throw_exception() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("1ACVD12345");
    }

    @Test(expected = InvalidRequest.class)
    public void organisation_identifier_contains_only_letters_should_throw_exception() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("ABCDEFG");
    }

    @Test(expected = InvalidRequest.class)
    public void organisation_identifier_contains_only_digits_should_throw_exception() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("1234567");
    }

    @Test(expected = InvalidRequest.class)
    public void organisation_identifier_contains_speacial_character_should_throw_exception() {
        organisationCreationRequestValidator.validateOrganisationIdentifier("1N34VB*");
    }

}