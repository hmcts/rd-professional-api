package uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver;

public interface SubjectResolver<T extends Subject> {
    T getTokenDetails(String bearerToken);
}