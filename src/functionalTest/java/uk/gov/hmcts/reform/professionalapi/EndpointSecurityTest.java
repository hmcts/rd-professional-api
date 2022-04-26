package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

@SerenityTest
@SpringBootTest
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
class EndpointSecurityTest extends AuthorizationFunctionalTest {

    @Test
    void should_allow_unauthenticated_requests_to_welcome_message_and_return_200_response_code() {
        assertThat(professionalApiClient.getWelcomePage()).contains("Welcome");
    }

    @Test
    void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() {
        assertThat(professionalApiClient.getHealthPage()).contains("UP");
    }
}
