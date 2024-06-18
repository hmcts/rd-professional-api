package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationMfaStatusController;
import uk.gov.hmcts.reform.professionalapi.controller.response.MfaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.pact.util.PactUtils.getOrgWithMfaStatus;
import static uk.gov.hmcts.reform.professionalapi.pact.util.PactUtils.getProfessionalUser;

@Provider("referenceData_organisation_mfa")
@Import(OrganisationalExternalControllerProviderTestConfiguration.class)
@IgnoreNoPactsToVerify
public class OrganisationalMfaStatusControllerProviderTest extends MockMvcProviderTest {

    @Autowired
    OrganisationMfaStatusController organisationMfaStatusController;

    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    @Autowired
    UserAttributeService userAttributeService;


    @Override
    void setController() {
        testTarget.setControllers(organisationMfaStatusController);
    }

    //MFA pact get api test
    @State("MFA exists for Organisation")
    public void setUpUserWithOrganisation() {
        Organisation organisation = getOrgWithMfaStatus();
        ProfessionalUser professionalUser = getProfessionalUser();
        professionalUser.setOrganisation(organisation);

        MfaStatusResponse mfaStatusResponse = new MfaStatusResponse();
        mfaStatusResponse.setMfa("EMAIL");

        when(professionalUserRepository.findByUserIdentifier(anyString())).thenReturn(professionalUser);
    }
}
