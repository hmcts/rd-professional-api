package uk.gov.hmcts.reform.professionalapi;

import java.util.ArrayList;
import java.util.List;

import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.ACCESS_EXCEPTION;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.util.CustomSerenityRunner.getFeatureFlagName;
import static uk.gov.hmcts.reform.professionalapi.util.FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD;

@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
public class RetrieveMinimalOrganisationsInfoTest extends AuthorizationFunctionalTest {

    private boolean activeOrgsExternalEnabled;

    private static final String STATUS_PARAM_INVALID_MESSAGE =
        "Invalid status param provided, only Active status is allowed";

    private List<OrganisationMinimalInfoResponse> activeOrgs = new ArrayList<>();
    private List<OrganisationMinimalInfoResponse> pendingOrgs = new ArrayList<>();
    private List<OrganisationMinimalInfoResponse> noAddressOrgs = new ArrayList<>();

    private OrganisationMinimalInfoResponse organisationEntityResponse1;
    private OrganisationMinimalInfoResponse organisationEntityResponse2;
    private OrganisationMinimalInfoResponse organisationEntityResponse3;
    private OrganisationMinimalInfoResponse organisationEntityResponse4;
    private OrganisationMinimalInfoResponse organisationEntityResponse5;
    private OrganisationMinimalInfoResponse organisationEntityResponse6;
    private String orgIdentifier1;
    private String orgIdentifier2;
    private String orgIdentifier3;
    private String orgIdentifier4;
    private String orgIdentifier5;
    private String orgIdentifier6;
    private List<String> validRoles = new ArrayList<>();
    private List<String> orgResponseInfo = new ArrayList<>();

    public static final String mapKey = "OrganisationExternalController"
        + ".retrieveOrganisationsByStatusWithAddressDetailsOptional";

    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = true)
    public void should_retrieve_organisations_info_with_200_with_correct_roles_and_status_active() {
        setUpTestData();
        List<OrganisationMinimalInfoResponse> responseList = (List<OrganisationMinimalInfoResponse>)
            professionalApiClient.retrieveAllActiveOrganisationsWithMinimalInfo(
                bearerToken, HttpStatus.OK, IdamStatus.ACTIVE.toString(), true);

        responseList.forEach(org -> orgResponseInfo.addAll(getOrganisationInfo(org)));

        assertThat(orgResponseInfo)
            .contains(
                activeOrgs.get(0).getOrganisationIdentifier(),
                activeOrgs.get(0).getName(),
                activeOrgs.get(0).getContactInformation().get(0).getAddressLine1(),
                activeOrgs.get(1).getOrganisationIdentifier(),
                activeOrgs.get(1).getName(),
                activeOrgs.get(1).getContactInformation().get(0).getAddressLine1(),
                noAddressOrgs.get(0).getOrganisationIdentifier(),
                noAddressOrgs.get(0).getName(),
                noAddressOrgs.get(1).getOrganisationIdentifier(),
                noAddressOrgs.get(1).getName())
            .doesNotContain(
                pendingOrgs.get(0).getOrganisationIdentifier(),
                pendingOrgs.get(0).getName(),
                pendingOrgs.get(1).getOrganisationIdentifier(),
                pendingOrgs.get(1).getName());
    }

    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = false)
    public void should_retrieve_403_when_API_toggled_off() {
        setUpTestData();

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
        // invite new user having invalid roles
        List<String> userRoles = new ArrayList<>();
        userRoles.add("caseworker");
        inviteNewUser(userRoles);
        validateErrorResponse((ErrorResponse) professionalApiClient
                .retrieveAllActiveOrganisationsWithMinimalInfo(bearerToken,
                    HttpStatus.FORBIDDEN, ACTIVE.toString(), true),
            ACCESS_EXCEPTION.getErrorMessage(), ACCESS_IS_DENIED_ERROR_MESSAGE);
    }

    public void setUpTestData() {
        //create 2 active orgs for validating response has these 2 orgs present
        createActiveOrganisation1();
        createActiveOrganisation2();

        activeOrgs.add(organisationEntityResponse1);
        activeOrgs.add(organisationEntityResponse2);

        //create 2 pending orgs for validating response doesn't have 2 orgs present
        createPendingOrganisation1();
        createPendingOrganisation2();

        pendingOrgs.add(organisationEntityResponse3);
        pendingOrgs.add(organisationEntityResponse4);


        //create 2 active orgs for validating response doesn't have 2 contact address present for 2 orgs
        createActiveOrganisation3WithAddressRequiredFalse();
        createActiveOrganisation4WithAddressRequiredFalse();

        noAddressOrgs.add(organisationEntityResponse5);
        noAddressOrgs.add(organisationEntityResponse6);

        //invite user under org1 with valid roles and who is active
        inviteNewUser(getValidRoleList());

    }

    public void inviteNewUser(List<String> roles) {
        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        String email = idamOpenIdClient.nextUserEmail();
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        newUserCreationRequest.setEmail(email);
        newUserCreationRequest.setRoles(roles);

        idamOpenIdClient.createUser(hmctsAdmin, email, newUserCreationRequest.getFirstName(),
            newUserCreationRequest.getLastName());
        bearerToken = professionalApiClient.getMultipleAuthHeaders(idamOpenIdClient.getOpenIdToken(email));

        professionalApiClient.addNewUserToAnOrganisation(
            orgIdentifier1 == null ? createActiveOrganisation1() :
                orgIdentifier1, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

    }

    public List<String> getValidRoleList() {
        if (CollectionUtils.isEmpty(validRoles)) {
            validRoles = new ArrayList<>();
            validRoles.add(puiUserManager);
            validRoles.add(puiCaseManager);
            validRoles.add(puiOrgManager);
            validRoles.add(puiFinanceManager);
            validRoles.add(puiCaa);
        }
        return validRoles;
    }

    public String createActiveOrganisation1() {
        String orgName1 = randomAlphabetic(7);
        OrganisationCreationRequest organisationCreationRequest = createMinimalOrganisationRequest(orgName1);
        orgIdentifier1 = createAndctivateOrganisationWithGivenRequest(
            organisationCreationRequest, hmctsAdmin);

        organisationEntityResponse1 = new OrganisationMinimalInfoResponse(
            generateOrganisation(organisationCreationRequest, orgIdentifier1), true);
        return orgIdentifier1;
    }

    public void createActiveOrganisation2() {
        String orgName2 = randomAlphabetic(7);
        OrganisationCreationRequest organisationCreationRequest = createMinimalOrganisationRequest(orgName2);
        orgIdentifier2 = createAndctivateOrganisationWithGivenRequest(
            organisationCreationRequest, hmctsAdmin);

        organisationEntityResponse2 = new OrganisationMinimalInfoResponse(
            generateOrganisation(organisationCreationRequest, orgIdentifier2), true);
    }

    public void createPendingOrganisation1() {
        String orgName3 = randomAlphabetic(7);
        OrganisationCreationRequest organisationCreationRequest = createMinimalOrganisationRequest(orgName3);
        orgIdentifier3 = createOrganisation(organisationCreationRequest);

        organisationEntityResponse3 = new OrganisationMinimalInfoResponse(
            generateOrganisation(organisationCreationRequest, orgIdentifier3), true);
    }

    public void createPendingOrganisation2() {
        String orgName4 = randomAlphabetic(7);
        OrganisationCreationRequest organisationCreationRequest = createMinimalOrganisationRequest(orgName4);
        orgIdentifier4 = createOrganisation(organisationCreationRequest);

        organisationEntityResponse4 = new OrganisationMinimalInfoResponse(
            generateOrganisation(organisationCreationRequest, orgIdentifier4), true);
    }

    public void createActiveOrganisation3WithAddressRequiredFalse() {
        String orgName5 = randomAlphabetic(7);
        OrganisationCreationRequest organisationCreationRequest = createMinimalOrganisationRequest(orgName5);
        orgIdentifier5 = createAndctivateOrganisationWithGivenRequest(
            organisationCreationRequest, hmctsAdmin);

        organisationEntityResponse5 = new OrganisationMinimalInfoResponse(
            generateOrganisation(organisationCreationRequest, orgIdentifier5), false);
    }

    public void createActiveOrganisation4WithAddressRequiredFalse() {
        String orgName6 = randomAlphabetic(7);
        OrganisationCreationRequest organisationCreationRequest = createMinimalOrganisationRequest(orgName6);
        orgIdentifier6 = createAndctivateOrganisationWithGivenRequest(
            organisationCreationRequest, hmctsAdmin);

        organisationEntityResponse6 = new OrganisationMinimalInfoResponse(
            generateOrganisation(organisationCreationRequest, orgIdentifier6), false);
    }

    public String createOrganisation(OrganisationCreationRequest organisationCreationRequest) {
        return (String) professionalApiClient.createOrganisation(
            organisationCreationRequest).get("organisationIdentifier");
    }

    public OrganisationCreationRequest createMinimalOrganisationRequest(String organisationName) {
        return someMinimalOrganisationRequest()
            .name(organisationName).status(ACTIVE.name())
            .sraId(randomAlphabetic(10)).build();
    }

    public Organisation generateOrganisation(OrganisationCreationRequest creationRequest, String orgId) {
        Organisation organisation = new Organisation(creationRequest.getName(),
            OrganisationStatus.valueOf(creationRequest.getStatus()),
            creationRequest.getSraId(), creationRequest.getCompanyNumber(),
            Boolean.valueOf(creationRequest.getSraRegulated()), creationRequest.getCompanyUrl());

        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1(creationRequest.getContactInformation().get(0).getAddressLine1());

        organisation.setContactInformations(singletonList(contactInformation));
        organisation.setOrganisationIdentifier(orgId);

        return organisation;
    }

    public List<String> getOrganisationInfo(OrganisationMinimalInfoResponse org) {
        List<String> details = new ArrayList<>();
        if (null != org.getContactInformation().get(0).getAddressLine1()) {
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
