package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@Slf4j
public class ProfessionalExternalUserFunctionalTest extends AuthorizationFunctionalTest{

    /*
    Create Organisation
    Approve Org
    Invite User to Org
    Find user by Org
    Get Org
    Get PBA
     */

    @Test
    public void testInternalUserScenario() {

        //create and approve org already taken care in AuthorizationFunctionalTest in BeforeTest
        //inviteUserScenarios();
        //findUsersByOrganisationScenarios();
        //findOrganisationScenarios();
        //retrieveOrganisationPbaScenarios();

    }
}
