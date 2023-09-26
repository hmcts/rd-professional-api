package uk.gov.hmcts.reform.professionalapi;

import io.restassured.parsing.Parser;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.lib.client.response.S2sClient;
import uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient;
import uk.gov.hmcts.reform.professionalapi.config.Oauth2;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

@ContextConfiguration(classes = {TestConfigProperties.class, Oauth2.class})
@ComponentScan("uk.gov.hmcts.reform.professionalapi")
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
public class AuthorizationFunctionalTest {

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${targetInstance}")
    protected String professionalApiUrl;

    @Value("${prd.security.roles.hmcts-admin}")
    protected String hmctsAdmin;

    @Value("${prd.security.roles.pui-user-manager}")
    protected String puiUserManager;

    @Value("${prd.security.roles.pui-organisation-manager}")
    protected String puiOrgManager;

    @Value("${prd.security.roles.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${prd.security.roles.pui-case-manager}")
    protected String puiCaseManager;

    @Value("${prd.security.roles.pui-caa}")
    protected String puiCaa;

    @Value("${prd.security.roles.caseworker-caa}")
    protected String caseworkerCaa;

    @Value("${prd.security.roles.prd-aac-system}")
    protected String systemUser;

    @Value("${prd.security.roles.caseworker}")
    protected String caseworker;

    @Value("${prd.security.roles.citizen}")
    protected String citizen;

    @Value("${resendInterval}")
    protected String resendInterval;

    protected static ProfessionalApiClient professionalApiClient;

    protected static IdamOpenIdClient idamOpenIdClient;

    @Autowired
    protected TestConfigProperties configProperties;

    protected static String  s2sToken;
    public static final String EMAIL = "EMAIL";
    public static final String CREDS = "CREDS";
    public static final String EMAIL_TEMPLATE = "freg-test-user-%s@prdfunctestuser.com";
    public static String email;
    public static String activeOrgId;
    public static String activeOrgIdForBearerTokens;
    public static NewUserCreationRequest bearerTokenUser;


    @PostConstruct
    public void beforeTestClass() {
        SerenityRest.useRelaxedHTTPSValidation();
        SerenityRest.setDefaultParser(Parser.JSON);

        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);

        if (null == s2sToken) {
            s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();
        }

        if (null == idamOpenIdClient) {
            idamOpenIdClient = new IdamOpenIdClient(configProperties);
        }
        professionalApiClient = new ProfessionalApiClient(
            professionalApiUrl,
            s2sToken, idamOpenIdClient);
    }

    protected String createAndUpdateOrganisationToActive(String role) {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        return activateOrganisation(response, role);
    }

    protected String createAndUpdateOrganisationToActive(String role,
                                                         OrganisationCreationRequest organisationCreationRequest) {

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        return activateOrganisation(response, role);
    }

    protected String createAndUpdateOrganisationToActiveForV2(String role,
                                                OrganisationOtherOrgsCreationRequest organisationCreationRequest) {

        Map<String, Object> response = professionalApiClient.createOrganisationV2(organisationCreationRequest);
        return activateOrganisationV2(response, role);
    }

    protected String createAndctivateOrganisationWithGivenRequest(
            OrganisationCreationRequest organisationCreationRequest, String role) {
        Map<String, Object> organisationCreationResponse = professionalApiClient
                .createOrganisation(organisationCreationRequest);
        String organisationIdentifier = (String) organisationCreationResponse.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationCreationRequest, role, organisationIdentifier);
        return organisationIdentifier;
    }

    protected String createAndActivateOrganisationWithGivenRequestV2(
            OrganisationOtherOrgsCreationRequest organisationCreationRequest, String role) {
        Map<String, Object> organisationCreationResponse = professionalApiClient
                .createOrganisationV2(organisationCreationRequest);
        String organisationIdentifier = (String) organisationCreationResponse.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisationV2(organisationCreationRequest, role, organisationIdentifier);
        return organisationIdentifier;
    }

    protected String activateOrganisation(Map<String, Object> organisationCreationResponse, String role) {
        String organisationIdentifier = (String) organisationCreationResponse.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationIdentifier,role);
        return organisationIdentifier;
    }

    protected String activateOrganisationV2(Map<String, Object> organisationCreationResponse, String role) {
        String organisationIdentifier = (String) organisationCreationResponse.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisationV2(organisationIdentifier,role);
        return organisationIdentifier;
    }

    protected NewUserCreationRequest createUserRequest(List<String> userRoles) {

        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someFirstName";
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .build();
        return userCreationRequest;
    }

    public UserProfileUpdatedData getUserStatusUpdateRequest(IdamStatus status) {
        UserProfileUpdatedData data = new UserProfileUpdatedData();
        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(status.name());
        return data;
    }

    public Map getActiveUser(List<Map> professionalUsersResponses) {

        Map activeUserMap = null;

        for (Map userMap : professionalUsersResponses) {
            if (userMap.get("idamStatus").equals(IdamStatus.ACTIVE.name())) {
                activeUserMap = userMap;
            }
        }
        return activeUserMap;
    }

    public Map getUserById(List<Map<String,Object>> professionalUsersResponses, String userId) {

        Map activeUserMap = null;

        for (Map userMap : professionalUsersResponses) {
            if (userMap.get("userIdentifier").equals(userId)) {
                activeUserMap = userMap;
            }
        }
        return activeUserMap;
    }

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10));
    }

    public List<String> addRoles(String role) {
        List<String> roles = new ArrayList<String>();
        roles.add(role);
        return roles;
    }

    public List<String> superUserRoles() {
        List<String> superUserRoles = new ArrayList<String>();
        superUserRoles.add("pui-case-manager");
        superUserRoles.add("pui-user-manager");
        superUserRoles.add("pui-organisation-manager");
        superUserRoles.add("pui-finance-manager");
        //superUserRoles.add("caseworker-divorce-financialremedy");
        //superUserRoles.add("caseworker-divorce-financialremedy-solicitor");
        //superUserRoles.add("caseworker-divorce-solicitor");
        //superUserRoles.add("caseworker-divorce");
        //superUserRoles.add("caseworker");
        //superUserRoles.add("caseworker-probate");
        //superUserRoles.add("caseworker-probate-solicitor");
        //superUserRoles.add("caseworker-publiclaw");
        //superUserRoles.add("caseworker-publiclaw-solicitor");
        //superUserRoles.add("caseworker-ia-legalrep-solicitor");
        //superUserRoles.add("caseworker-ia");
        return superUserRoles;
    }

    public String generateBearerToken(String bearer, String role) {
        if (null == bearer) {
            bearerTokenUser = createUserRequest(asList(role));

            bearer = professionalApiClient.getBearerTokenExternal(role, bearerTokenUser.getFirstName(),
                    bearerTokenUser.getLastName(), bearerTokenUser.getEmail());

            professionalApiClient.addNewUserToAnOrganisation(activeOrgIdForBearerTokens,
                    hmctsAdmin, bearerTokenUser, HttpStatus.CREATED);
            email = bearerTokenUser.getEmail();
        }
        return bearer;
    }

    public String searchUserStatus(String orgIdentifier, String userId) {

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        return professionalUsersResponses.stream()
                .filter(user -> ((String) user.get("userIdentifier")).equalsIgnoreCase(userId))
                .map(user -> (String) user.get("idamStatus"))
                .collect(Collectors.toList()).get(0);
    }

    public void responseValidate(Map<String, Object> orgResponse) {

        orgResponse.forEach((k,v) -> {

            if ("organisationIdentifier".equals(k) && "http_status".equals(k)
                    && "name".equals(k) &&  "status".equals(k)
                    && "superUser".equals(k) && "paymentAccount".equals(k)) {

                Assertions.assertThat(v.toString()).isNotEmpty();
                Assertions.assertThat(v.toString().contains("Ok"));
                Assertions.assertThat(v.toString().contains("some-org-name"));
                Assertions.assertThat(v.toString().equals("ACTIVE"));
                Assertions.assertThat(v.toString()).isNotEmpty();
                Assertions.assertThat(v.toString()).contains("dateCreated");
                Assertions.assertThat(v.toString()).contains("dateApproved");
            }

        });

    }

    public void responseValidateV2(Map<String, Object> orgResponse) {

        orgResponse.forEach((k,v) -> {

            if ("organisationIdentifier".equals(k) && "http_status".equals(k)
                    && "name".equals(k) &&  "status".equals(k)
                    && "superUser".equals(k) && "paymentAccount".equals(k)) {

                Assertions.assertThat(v.toString()).isNotEmpty();
                Assertions.assertThat(v.toString().contains("Ok"));
                Assertions.assertThat(v.toString().contains("some-org-name"));
                Assertions.assertThat(v.toString().equals("ACTIVE"));
                Assertions.assertThat(v.toString()).isNotEmpty();
                Assertions.assertThat(v.toString()).contains("dateCreated");
                Assertions.assertThat(v.toString()).contains("dateApproved");
                Assertions.assertThat(v.toString()).contains("Doctor");
                Assertions.assertThat(v.toString()).contains("testKey");
                Assertions.assertThat(v.toString()).contains("testValue");

            }

        });

    }

    public UserProfileUpdatedData deleteRoleRequest(String role) {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName = new RoleName(role);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName);
        userProfileUpdatedData.setRolesDelete(roles);
        return userProfileUpdatedData;
    }

    public UserProfileUpdatedData addRoleRequest(String role) {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName = new RoleName(role);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName);
        userProfileUpdatedData.setRolesAdd(roles);
        return userProfileUpdatedData;
    }

    public void validatePbaResponse(Map<String, Object> response) {
        List<String> pbaList = (List)((Map)response.get("organisationEntityResponse")).get("paymentAccount");
        assertThat(pbaList).hasSize(3);
    }

    public void validateSingleOrgResponse(Map<String, Object> response, String status) {

        Assertions.assertThat(response.size()).isPositive();
        assertThat(response.get("organisationIdentifier")).isNotNull();
        assertThat(response.get("name")).isNotNull();
        assertThat(response).containsEntry("status", status);
        assertThat(response.get("sraId")).isNotNull();
        assertThat(response.get("sraRegulated")).isNotNull();
        assertThat(response.get("companyNumber")).isNotNull();
        assertThat(response.get("companyUrl")).isNotNull();
        assertThat(response.get("superUser")).isNotNull();
        assertThat(response.get("paymentAccount")).isNotNull();
        assertThat(response.get("contactInformation")).isNotNull();
        verifyContactInfoCreatedDateSorting(response.get("contactInformation"));

    }

    public void validateSingleOrgResponseForV2(Map<String, Object> response, String status) {

        Assertions.assertThat(response.size()).isPositive();
        assertThat(response.get("organisationIdentifier")).isNotNull();
        assertThat(response.get("name")).isNotNull();
        assertThat(response).containsEntry("status", status);
        assertThat(response.get("sraId")).isNotNull();
        assertThat(response.get("sraRegulated")).isNotNull();
        assertThat(response.get("companyNumber")).isNotNull();
        assertThat(response.get("companyUrl")).isNotNull();
        assertThat(response.get("superUser")).isNotNull();
        assertThat(response.get("paymentAccount")).isNotNull();
        assertThat(response.get("contactInformation")).isNotNull();
        assertThat(response.get("orgTypeKey")).isNotNull();
        assertThat(response.get("orgAttributes")).isNotNull();
        verifyContactInfoCreatedDateSorting(response.get("contactInformation"));

    }

    public void validateRetrievedUsers(Map<String, Object> searchResponse, String expectedStatus,
                                       Boolean rolesReturned) {
        assertThat(searchResponse.get("users")).asList().isNotEmpty();
        assertThat(searchResponse.get("organisationIdentifier")).isNotNull();
        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");

        professionalUsersResponses.forEach(user -> {
            assertThat(user.get("idamStatus")).isNotNull();
            assertThat(user.get("userIdentifier")).isNotNull();
            assertThat(user.get("firstName")).isNotNull();
            assertThat(user.get("lastName")).isNotNull();
            assertThat(user.get("email")).isNotNull();
            if (!expectedStatus.equals("any")) {
                assertThat(user.get("idamStatus").equals(expectedStatus));
            }
            if (rolesReturned) {
                if (user.get("idamStatus").equals(IdamStatus.ACTIVE.toString())) {
                    assertThat(user.get("roles")).isNotNull();
                } else {
                    assertThat(user.get("roles")).isNull();
                }
            }
        });
    }

    private void verifyContactInfoCreatedDateSorting(Object contactInformation) {
        var contactInformationResponse = (List<HashMap>) contactInformation;
        assertEquals("addressLine1", contactInformationResponse.get(0).get("addressLine1"));
        assertEquals("addressLine2", contactInformationResponse.get(0).get("addressLine2"));
        assertEquals("addressLine3", contactInformationResponse.get(0).get("addressLine3"));
        assertEquals("uprn1", contactInformationResponse.get(0).get("uprn"));
        assertNotNull(contactInformationResponse.get(0).get("created"));
        assertNotNull(contactInformationResponse.get(0).get("addressId"));
        assertEquals("addressLine3", contactInformationResponse.get(0).get("addressLine3"));
        assertEquals("addressLine3", contactInformationResponse.get(0).get("addressLine3"));
        assertEquals("addLine1", contactInformationResponse.get(1).get("addressLine1"));
        assertEquals("addLine2", contactInformationResponse.get(1).get("addressLine2"));
        assertEquals("addLine3", contactInformationResponse.get(1).get("addressLine3"));
        assertEquals("uprn", contactInformationResponse.get(1).get("uprn"));
        assertNotNull(contactInformationResponse.get(1).get("created"));
        assertNotNull(contactInformationResponse.get(1).get("addressId"));
    }

}
