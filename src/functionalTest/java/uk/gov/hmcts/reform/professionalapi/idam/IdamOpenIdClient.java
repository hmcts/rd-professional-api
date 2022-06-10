package uk.gov.hmcts.reform.professionalapi.idam;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.gson.Gson;

import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.reform.lib.idam.IdamOpenId;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;


@Slf4j
public class IdamOpenIdClient extends IdamOpenId {

    private TestConfigProperties testConfig;

    private Gson gson = new Gson();

    private static String internalOpenIdTokenPrdAdmin;

    private static String sidamPassword;

    public IdamOpenIdClient(TestConfigProperties testConfig) {
        super(testConfig);
        this.testConfig = testConfig;
    }

    public int createSuperUserWithRetry(List<String> userRoles, String userEmail, String firstName,
                                        String lastName, String password) {
        //Generating a random user
        String userGroup = "";

        String id = UUID.randomUUID().toString();
        List<Role> roles = new ArrayList<>();
        userRoles.forEach(userRole -> {
            Role role = new Role(userRole);
            roles.add(role);
        });

        Group group = new Group(userGroup);

        User user = new User(userEmail, firstName, id, lastName, password, roles, group);

        String serializedUser = gson.toJson(user);
        log.info("serializedUser: " + serializedUser);
        Response createdUserResponse = SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .baseUri(testConfig.getIdamApiUrl())
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .body(serializedUser)
                .post("/testing-support/accounts")
                .andReturn();

        log.info("SIDAM createUser with retry response: " + createdUserResponse.getStatusCode());

        return createdUserResponse.getStatusCode();
    }

    public String getExternalOpenIdToken(String role, String firstName, String lastName, String email) {
        List<String> roles = new ArrayList<String>();
        roles.add(role);
        Map<String, String> userCreds = createUser(roles, email, firstName, lastName);
        return getOpenIdToken(userCreds.get(EMAIL), userCreds.get(CREDS));
    }

    public String getExternalOpenIdTokenWithRetry(List<String> roles, String firstName, String lastName, String email) {
        String password = generateSidamPassword();
        int statusCode = createSuperUserWithRetry(roles, email, firstName, lastName, password);

        if (statusCode != 201) {
            return String.valueOf(statusCode);
        }

        Map<String, String> userCreds = new HashMap<>();
        userCreds.put(EMAIL, email);
        userCreds.put(CREDS, password);
        return getOpenIdToken(userCreds.get(EMAIL), userCreds.get(CREDS));
    }
}
