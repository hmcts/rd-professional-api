package uk.gov.hmcts.reform.professionalapi.authchecker.core.service;

import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.Subject;

import java.util.Set;

public class User extends Subject {

    private final Set<String> roles;

    public User(String principleId, Set<String> roles) {
        super(principleId);
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
