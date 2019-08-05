package uk.gov.hmcts.reform.professionalapi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.RequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

public class OrganisationCreationRequestValidatorTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    RequestValidator validator1;

    @Autowired
    RequestValidator validator2;

    @Autowired
    OrganisationCreationRequest orgCreateRequest;

    @Autowired
    OrganisationCreationRequestValidator organisationCreationRequestValidator;

    @Autowired
    Organisation org;

    @Before
    public void setUp() {
        organisationCreationRequestValidator =
                new OrganisationCreationRequestValidator(asList(validator1, validator2));
    }

    @Test
    public void test() {

    }
}
