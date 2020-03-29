package uk.gov.hmcts.reform.professionalapi.authchecker.core.service;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ServiceAndUserDetails extends org.springframework.security.core.userdetails.User {

    private final String serviceName;

    public ServiceAndUserDetails(String userName, String token, Collection<String> authorities, String serviceName) {
        super(userName, token, toGrantedAuthorities(authorities));
        this.serviceName = serviceName;
    }

    private static Collection<? extends GrantedAuthority> toGrantedAuthorities(Collection<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(toList());
    }

    public String getServiceName() {
        return serviceName;
    }
}

