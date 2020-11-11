package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;

import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
public class OrganisationCreationsTest extends AuthorizationFunctionalTest {

    @Test
    public void can_create_an_organisation() {
        assertThat(createAndUpdateOrganisationToActive(hmctsAdmin)).isNotEmpty();
    }

    @Test
    public void ac7_can_throw_Unauthorized_Error_code_without_service_token_create_an_organisation_401() {
        Response response =
                professionalApiClient.createOrganisationWithoutS2SToken(anOrganisationCreationRequest().build());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}