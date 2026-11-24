package uk.gov.hmcts.reform.professionalapi.security;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.TestPropertySource;

import java.util.stream.Stream;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.reform.professionalapi.security.BaseSecurityIntegrationTest.VALID_ISSUER_1;
import static uk.gov.hmcts.reform.professionalapi.security.BaseSecurityIntegrationTest.VALID_ISSUER_2;

@TestPropertySource(properties = {
    "idam.security.issuer-validation=true",
    "idam.security.allowed-issuers[0]=" + VALID_ISSUER_1,
    "idam.security.allowed-issuers[1]=" + VALID_ISSUER_2
})
public class JwtIssuerValidationEnabledIntegrationTest extends BaseSecurityIntegrationTest {

    private static Stream<Arguments> issuerValidationEnabledScenarios() {
        return Stream.of(
                Arguments.of(
                        "Scenario 1 - JWT Issuer validation is enabled And Valid primary issuer is accepted - 200",
                        VALID_ISSUER_1,
                        false,
                        OK.value()),

                Arguments.of(
                        "Scenario 2 - JWT Issuer validation is enabled And Valid secondary issuer is accepted - 200",
                        VALID_ISSUER_2,
                        false,
                        OK.value()),

                Arguments.of(
                        "Scenario 3 - JWT Issuer validation is enabled And Rogue issuer is rejected - 401",
                        ROGUE_ISSUER,
                        false,
                        UNAUTHORIZED.value()),

                Arguments.of(
                        "Scenario 4 - JWT Issuer validation is enabled And Missing issuer is rejected - 401",
                        null,
                        false,
                        UNAUTHORIZED.value()),

                Arguments.of(
                        "Scenario 5 - JWT Issuer validation is enabled And Empty issuer is rejected - 401",
                        "",
                        false,
                        UNAUTHORIZED.value()),

                Arguments.of(
                        "Scenario 6 - JWT Issuer validation is enabled "
                                + "And Issuer with trailing slash is rejected - 401",
                        VALID_ISSUER_1 + "/",
                        false,
                        UNAUTHORIZED.value()),

                Arguments.of(
                        "Scenario 7 - JWT Issuer validation is enabled "
                                + "And Issuer with different case is rejected - 401",
                        VALID_ISSUER_1.toUpperCase(),
                        false,
                        UNAUTHORIZED.value()),

                Arguments.of(
                        "Scenario 8 - JWT Issuer validation is enabled And Expired token is rejected - 401",
                        VALID_ISSUER_1,
                        true,
                        UNAUTHORIZED.value())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("issuerValidationEnabledScenarios")
    void issuerValidationEnabled_shouldReturnExpectedStatus(String scenario,
                                                            String jwtIssuer,
                                                            boolean tokenExpired,
                                                            int expectedStatusCode) throws Exception {

        RequestSpecification jwtRequestSpecification =
                tokenExpired
                        ? expiredJwt(jwtIssuer)
                        : unexpiredJwt(jwtIssuer);

        jwtRequestSpecification
                .when()
                .request()
                .with()
                .body(getObjectMapper().writeValueAsString(organisationUpdateRequest))
                .and().log().all()
                .put(format(UPDATE_ORG_URL, organisationIdentifier))
                .then().log().all()
                .assertThat()
                .statusCode(expectedStatusCode);
    }
}

