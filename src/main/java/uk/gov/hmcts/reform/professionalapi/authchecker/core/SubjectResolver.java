package uk.gov.hmcts.reform.professionalapi.authchecker.core;

public interface SubjectResolver<T extends Subject> {     T getTokenDetails(String bearerToken); }