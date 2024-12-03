package uk.gov.hmcts.reform.professionalapi;

import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityJUnit5Extension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({CustomSerenityJUnit5Extension.class, SerenityJUnit5Extension.class, SpringExtension.class})
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
