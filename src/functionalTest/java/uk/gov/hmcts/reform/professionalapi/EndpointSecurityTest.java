package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
public class EndpointSecurityTest extends AuthorizationFunctionalTest {

    @Test
    public void should_allow_unauthenticated_requests_to_welcome_message_and_return_200_response_code() {
        assertThat(professionalApiClient.getWelcomePage()).contains("Welcome");
    }

    @Test
    public void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() {
        assertThat(professionalApiClient.getHealthPage()).contains("UP");
    }
}
