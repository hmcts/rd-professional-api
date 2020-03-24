package uk.gov.hmcts.reform.professionalapi.authchecker.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class ServiceAndUserDetails extends org.springframework.security.core.userdetails.User {

    private final String servicename;

    public ServiceAndUserDetails(String username, String token, Collection<String> authorities, String servicename) {
        super(username, token, toGrantedAuthorities(authorities));
        this.servicename = servicename;
    }

    private static Collection<? extends GrantedAuthority> toGrantedAuthorities(Collection<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(toList());
    }

    public String getServicename() {
        return servicename;
    }
}

