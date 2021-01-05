package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@Slf4j
public class OrganisationRetrieveTest extends AuthorizationFunctionalTest {

    Map<String, Object> orgResponse;

    @BeforeAll
    public void setUp() {
        orgResponse = professionalApiClient.createOrganisation();
    }

    @Test
    @Ignore("convert to integration test once RDCC-2050 is completed")
    public void can_retrieve_all_organisations() {
        Map<String, Object> response = professionalApiClient.retrieveAllOrganisations(hmctsAdmin);
        assertThat(response.get("organisations")).isNotNull();
        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }
}
