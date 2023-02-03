package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.REVIEW;

@SerenityTest
@SpringBootTest
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@SuppressWarnings("unchecked")
class OrganisationInternalUserFunctionalTest  extends AuthorizationFunctionalTest {

    @Test
    @DisplayName("PRD Internal Delete Organisation with status REVIEW Test Scenarios")
    void testInternalOrganisationDeleteScenario() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        String statusMessage = "Company in review";

        professionalApiClient
                .updateOrganisationToReview(orgIdentifier, statusMessage, hmctsAdmin);

        Map<String, Object> orgResponse = professionalApiClient
                .retrieveOrganisationDetails(orgIdentifier, hmctsAdmin, OK);

        assertEquals(REVIEW.toString(), orgResponse.get("status"));
        assertEquals(statusMessage, orgResponse.get("statusMessage"));

        professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, NO_CONTENT);

        orgResponse =  professionalApiClient
                .retrieveOrganisationDetails(orgIdentifier, hmctsAdmin, NOT_FOUND);

        assertThat(orgResponse.get("status")).toString().contains(NOT_FOUND.toString());

    }


}