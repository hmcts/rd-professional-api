package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationNameUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.util.TestDataArguments;

import java.util.Map;
import java.util.stream.Stream;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class UpdateOrgNameIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private static Stream<Arguments> provideErrorScenariosResponses() {

        final TestDataArguments requestBodyIsNull =
                TestDataArguments.builder()
                        .organisationIdentifier(randomAlphanumeric(7).toUpperCase())
                        .statusCode("400")
                        .errorMessage("Required request body is missing")
                        .build();

        final TestDataArguments orgIdIsNull =
                TestDataArguments.builder()
                        .organisationNameUpdateRequest(new OrganisationNameUpdateRequest("some-Name"))
                        .statusCode("400")
                        .errorMessage("The given organisationIdentifier must be 7 Alphanumeric Characters")
                        .build();

        final TestDataArguments nonExistOrgId =
                TestDataArguments.builder()
                        .organisationNameUpdateRequest(new OrganisationNameUpdateRequest("some-Name"))
                        .organisationIdentifier(randomAlphanumeric(7).toUpperCase())
                        .statusCode("404")
                        .errorMessage("No Organisation was found with the given organisationIdentifier")
                        .build();

        final TestDataArguments invalidEmptyOrgName =
                TestDataArguments.builder()
                        .organisationNameUpdateRequest(new OrganisationNameUpdateRequest(""))
                        .validOrgIdIsRequired(true)
                        .orgName("")
                        .statusCode("400")
                        .errorMessage("Name is required")
                        .build();

        final TestDataArguments nullOrgName =
                TestDataArguments.builder()
                        .organisationNameUpdateRequest(new OrganisationNameUpdateRequest(null))
                        .validOrgIdIsRequired(true)
                        .statusCode("400")
                        .errorMessage("Name is required")
                        .build();

        return Stream.of(
                arguments(named("Should return 400 when request body is missing", requestBodyIsNull)),
                arguments(named("Should return 400 when org id is missing", orgIdIsNull)),
                arguments(named("Should return 404 when org is not found", nonExistOrgId)),
                arguments(named("Should return 400 when org name is empty", invalidEmptyOrgName)),
                arguments(named("Should return 400 when org name is null", nullOrgName)));
    }

    @Test
    void update_name_of_an_active_organisation_with_prd_admin_role_should_return_200() {
        String orgIdentifier = getOrganisationId();
        OrganisationNameUpdateRequest organisationNameUpdateRequest =
                new OrganisationNameUpdateRequest("updatedName");
        Map<String, Object> orgUpdatedNameResponse =
                professionalReferenceDataClient.updateOrgName(
                        organisationNameUpdateRequest, hmctsAdmin, orgIdentifier);
        assertThat(orgUpdatedNameResponse.get("http_status")).isEqualTo(200);
        Map<String, Object> responseBody =
                professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifier, hmctsAdmin);
        assertNotNull(responseBody.get("name"));
        assertThat(responseBody.get("name").toString()).isEqualTo("updatedName");
    }

    @DisplayName("Negative - Organisation name update failure scenarios")
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideErrorScenariosResponses")
    void update_name_with_invalid_request_should_return_error(TestDataArguments testDataArguments) {
        final String organisationIdentifier =
                testDataArguments.validOrgIdIsRequired()
                        ? getOrganisationId()
                        : testDataArguments.organisationIdentifier();
        Map<String, Object> updateResponse =
                professionalReferenceDataClient.updateOrgName(
                        testDataArguments.organisationNameUpdateRequest(), hmctsAdmin, organisationIdentifier);

        assertThat(updateResponse).containsEntry("http_status", testDataArguments.statusCode());
        assertThat(updateResponse.get("response_body").toString())
                .contains(testDataArguments.errorMessage());
    }

    private String getOrganisationId() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        return organisationIdentifier;
    }
}
