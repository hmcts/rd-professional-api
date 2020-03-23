package uk.gov.hmcts.reform.professionalapi.authchecker.core;

public abstract class Subject {

    private final String principal;

    public Subject(String principal) {
        this.principal = principal;
    }

    public String getPrincipal() {
        return principal;
    }
}
