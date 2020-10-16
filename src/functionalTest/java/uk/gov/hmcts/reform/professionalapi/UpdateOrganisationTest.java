package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class UpdateOrganisationTest extends AuthorizationFunctionalTest {

    @Test
    public void can_update_an_organisation() {
        professionalApiClient.updateOrganisation(activeOrgId, hmctsAdmin);
    }

    @Test
    public void can_throw_Unauthorized_Error_code_without_bearertoken_to_update_an_organisation_401() {
        professionalApiClient.updateOrganisationWithoutBearerToken(hmctsAdmin, activeOrgId,
                HttpStatus.UNAUTHORIZED);
    }
}
