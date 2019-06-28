package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class UpdateOrganisationTest extends AuthorizationFunctionalTest {


    @Test
    public void can_update_an_organisation() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        log.info("hmctsAdmin::" + hmctsAdmin);
        log.info("hmctsAdmin::" + hmctsAdmin.trim());
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);
    }
}
