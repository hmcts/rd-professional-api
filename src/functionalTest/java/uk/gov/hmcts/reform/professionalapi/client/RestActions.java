package uk.gov.hmcts.reform.professionalapi.client;

import java.util.UUID;

import org.springframework.http.HttpHeaders;

import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;

public class RestActions {

    private final HttpHeaders httpHeaders = new HttpHeaders();


   // public final static String CITIZEN_ID = "1";
   // public final static String CASEWORKER_ID = "2";
  //  public final static String AUTHENTICATED_USER_ID = "3";
  //  public final static String SOLICITOR_ID = "4";


    private  UserResolverBackdoor userRequestAuthorizer = null;// = new UserResolverBackdoor();

    public RestActions(UserResolverBackdoor userRequestAuthorizer) {

        this.userRequestAuthorizer = userRequestAuthorizer;

    }

    public RestActions withUserId(String userId) {
        httpHeaders.add("user-id", userId);

        return this;
    }


    public RestActions withAuthorizedUser(String userId) {
        String token = UUID.randomUUID().toString();
        userRequestAuthorizer.registerToken(token, userId);
        httpHeaders.add(UserRequestAuthorizer.AUTHORISATION, token);
        return this;
    }

    public String getAuthorizationToken(String userId) {
        String token = UUID.randomUUID().toString();
        userRequestAuthorizer.registerToken(token, userId);
       // httpHeaders.add(UserRequestAuthorizer.AUTHORISATION, token);
        return token;
    }


}
