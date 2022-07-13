package uk.gov.hmcts.reform.professionalapi.oidc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;

/**
 * This class is used to parse the JWT Access token and returns the user info with GrantedAuthorities.
 * If GrantedAuthorities present in the token request will pass to the respective controller api methods
 * otherwise it displays unauthorised error message .
 *
 */
@Component
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    public static final String TOKEN_NAME = "tokenName";

    private final IdamRepository idamRepository;

    private UserInfo userInfo;

    @Autowired
    public JwtGrantedAuthoritiesConverter(IdamRepository idamRepository) {
        this.idamRepository = idamRepository;
    }

    /**
     * This method is used to parse the JWT Access token and returns the user info with authorities.
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (Boolean.TRUE.equals(jwt.containsClaim(TOKEN_NAME)) && jwt.getClaim(TOKEN_NAME).equals(ACCESS_TOKEN)) {
            userInfo = idamRepository.getUserInfo(jwt.getTokenValue());
            authorities = extractAuthorityFromClaims(userInfo.getRoles());


        }
        return authorities;
    }

    private List<GrantedAuthority> extractAuthorityFromClaims(List<String> roles) {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public  UserInfo getUserInfo() {

        return userInfo;
    }

}
