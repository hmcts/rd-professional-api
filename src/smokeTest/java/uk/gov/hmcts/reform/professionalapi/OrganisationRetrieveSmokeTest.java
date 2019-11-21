package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringIntegrationSerenityRunner.class)
@Slf4j
public class OrganisationRetrieveSmokeTest extends AuthorizationFunctionalSmokeTest {

    @Test
    public void can_retrieve_a_single_organisation() {

        professionalApiClient.retrieveOrganisationDetails("AB2345G",hmctsAdmin);

    }

}
