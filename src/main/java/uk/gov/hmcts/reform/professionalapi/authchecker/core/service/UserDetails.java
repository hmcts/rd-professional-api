package uk.gov.hmcts.reform.professionalapi.authchecker.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class UserDetails extends org.springframework.security.core.userdetails.User {

    public UserDetails(String username, String token, Collection<String> authorities) {
        super(username, token, toGrantedAuthorities(authorities));
    }

    private static Collection<? extends GrantedAuthority> toGrantedAuthorities(Collection<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(toList());
    }
}