package uk.gov.hmcts.reform.professionalapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.restassured.specification.RequestSpecification;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.ACCESS_EXCEPTION;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.util.CustomSerenityRunner.getFeatureFlagName;
import static uk.gov.hmcts.reform.professionalapi.util.FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD;

@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
public class RetrieveMinimalOrganisationsInfoTest extends AuthorizationFunctionalTest {

    private List<String> orgResponseInfo = new ArrayList<>();

    public static final String mapKey = "OrganisationExternalController"
            + ".retrieveOrganisationsByStatusWithAddressDetailsOptional";

    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = true)
    public void should_retrieve_organisations_info_with_200() {
        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someName";
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        RequestSpecification bearerToken =
                professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .build();

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest()
                .name(randomAlphabetic(7)).status(ACTIVE.name())
                .sraId(randomAlphabetic(10))
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("1A Address Line")
                        .build())).build();

        String orgIdentifier = createAndctivateOrganisationWithGivenRequest(
                organisationCreationRequest, hmctsAdmin);

        professionalApiClient
                .addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        List<OrganisationMinimalInfoResponse> responseWithContactInfo = (List<OrganisationMinimalInfoResponse>)
                professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(
                        bearerToken, HttpStatus.OK, IdamStatus.ACTIVE.toString(), true);

        responseWithContactInfo.forEach(org -> orgResponseInfo.addAll(getOrganisationInfo(org)));

        assertThat(orgResponseInfo).contains(
                organisationCreationRequest.getName(),
                organisationCreationRequest.getContactInformation().get(0).getAddressLine1(),
                orgIdentifier);
    }

    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = false)
    public void should_retrieve_403_when_API_toggled_off() {
        String exceptionMessage = getFeatureFlagName().concat(SPACE).concat(FORBIDDEN_EXCEPTION_LD);
        validateErrorResponse((ErrorResponse) professionalApiClient
                        .retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken,
                                HttpStatus.FORBIDDEN, ACTIVE.toString(), true),
                exceptionMessage,
                exceptionMessage);
    }

    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = true)
    public void should_fail_to_retrieve_organisations_info_with_403_with_incorrect_roles_and_status_active() {
        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someName";
        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker");

        RequestSpecification bearerToken =
                professionalApiClient.getMultipleAuthHeadersExternal("caseworker", firstName, lastName, userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .build();

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest()
                .name(randomAlphabetic(7)).status(ACTIVE.name())
                .sraId(randomAlphabetic(10)).build();

        String orgIdentifier = createAndctivateOrganisationWithGivenRequest(organisationCreationRequest, hmctsAdmin);

        professionalApiClient
                .addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        validateErrorResponse((ErrorResponse) professionalApiClient
                        .retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken,
                                HttpStatus.FORBIDDEN, ACTIVE.toString(), true),
                ACCESS_EXCEPTION.getErrorMessage(), ACCESS_IS_DENIED_ERROR_MESSAGE);
    }


    public List<String> getOrganisationInfo(OrganisationMinimalInfoResponse org) {
        List<String> details = new ArrayList<>();
        if (null != org.getContactInformation() && !org.getContactInformation().isEmpty()) {
            details.add(org.getContactInformation().get(0).getAddressLine1());
        }
        details.add(org.getName());
        details.add(org.getOrganisationIdentifier());
        return details;
    }

    public void validateErrorResponse(ErrorResponse errorResponse, String expectedErrorMessage,
                                      String expectedErrorDescription) {
        assertThat(errorResponse.getErrorDescription()).isEqualTo(expectedErrorDescription);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(expectedErrorMessage);
    }
}