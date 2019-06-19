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
        roleToTokenMap.put(CASEWORKER_ID ,"Bearer caseworker-2");
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
