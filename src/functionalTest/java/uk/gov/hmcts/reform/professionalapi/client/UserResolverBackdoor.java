package uk.gov.hmcts.reform.professionalapi.client;

import com.google.common.collect.ImmutableSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.auth.checker.core.SubjectResolver;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.AuthCheckerException;
import uk.gov.hmcts.reform.auth.checker.core.user.User;


@Component
@Primary
public class UserResolverBackdoor implements SubjectResolver<User> {

    private final ConcurrentHashMap<String, User> tokenToUserMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, String> roleToTokenMap = new ConcurrentHashMap<>();

    public final static String HMCTS_ADMIN_ID = "1";
    public final static String CASEWORKER_ID = "2";
    public final static String PUI_FINANCE_MANAGER_ID = "3";
    public final static String PUI_ORG_MANAGER_ID = "4";
    public final static String PUI_USER_MANAGER_ID = "5";

    public UserResolverBackdoor() {

        tokenToUserMap.put("Bearer hmctsuser-1", new User(HMCTS_ADMIN_ID, ImmutableSet.of("hmcts-admin")));
        tokenToUserMap.put("Bearer caseworker-2", new User(CASEWORKER_ID, ImmutableSet.of("pui-case-manager", "SuperUser")));
        tokenToUserMap.put("Bearer financemanager-3", new User(PUI_FINANCE_MANAGER_ID, ImmutableSet.of("pui-finance-manager")));
        tokenToUserMap.put("Bearer orgmanager-4", new User(PUI_ORG_MANAGER_ID, ImmutableSet.of("pui-organisation-manager")));
        tokenToUserMap.put("Bearer usermanager-5", new User(PUI_USER_MANAGER_ID, ImmutableSet.of("pui-user-manager")));

        roleToTokenMap.put(HMCTS_ADMIN_ID ,"Bearer hmctsuser-1");
        roleToTokenMap.put(CASEWORKER_ID ,"Bearer pui-case-manager-eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJzdXBlci51c2VyQGhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjZiYTdkYTk4LTRjMGYtNDVmNy04ZjFmLWU2N2NlYjllOGI1OCIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiI0NjAzYjVhYS00Y2ZhLTRhNDQtYWQzZC02ZWI0OTI2YjgxNzYiLCJhdWQiOiJteV9yZWZlcmVuY2VfZGF0YV9jbGllbnRfaWQiLCJuYmYiOjE1NTk4OTgxNzMsImdyYW50X3R5cGUiOiJhdXRob3JpemF0aW9uX2NvZGUiLCJzY29wZSI6WyJhY3IiLCJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJjcmVhdGUtdXNlciIsImF1dGhvcml0aWVzIl0sImF1dGhfdGltZSI6MTU1OTg5ODEzNTAwMCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1NTk5MjY5NzMsImlhdCI6MTU1OTg5ODE3MywiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IjgxN2ExNjE0LTVjNzAtNGY4YS05OTI3LWVlYjFlYzJmYWU4NiJ9.RLJyLEKldHeVhQEfSXHhfOpsD_b8dEBff7h0P4nZVLVNzVkNoiPdXYJwBTSUrXl4pyYJXEhdBwkInGp3OfWQKhHcp73_uE6ZXD0eIDZRvCn1Nvi9FZRyRMFQWl1l3Dkn2LxLMq8COh1w4lFfd08aj-VdXZa5xFqQefBeiG_xXBxWkJ-nZcW3tTXU0gUzarGY0xMsFTtyRRilpcup0XwVYhs79xytfbq0WklaMJ-DBTD0gux97KiWBrM8t6_5PUfMDBiMvxKfRNtwGD8gN8Vct9JUgVTj9DAIwg0KPPm1rEETRPszYI2wWvD2lpH2AwUtLBlRDANIkN9SdfiHSETvoQ");
        roleToTokenMap.put(PUI_FINANCE_MANAGER_ID,"Bearer financemanager-3");
        roleToTokenMap.put(PUI_ORG_MANAGER_ID,"Bearer orgmanager-4");
        roleToTokenMap.put(PUI_USER_MANAGER_ID,"Bearer usermanager-5");
    }

    @Override
    public User getTokenDetails(String token) {
        User user = tokenToUserMap.get(token);

        if (user == null) {
            throw new AuthCheckerException("Token not found");
        }

        return user;
    }

    public void registerToken(String token, String userId) {
        User user = tokenToUserMap.values().stream().filter(u -> u.getPrincipal().equals(userId)).findFirst().get();
        tokenToUserMap.put(token, user);
    }

    public ConcurrentHashMap<String, String> getRoleToTokenMap() {

        return roleToTokenMap;
    }

    public static String getBearerAuthorizationHeader(String roleId) {

        return roleToTokenMap.get(roleId);
    }
}
