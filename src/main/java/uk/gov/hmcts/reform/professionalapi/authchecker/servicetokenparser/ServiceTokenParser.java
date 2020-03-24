package uk.gov.hmcts.reform.professionalapi.authchecker.servicetokenparser;

import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.ServiceTokenParsingException;

public interface ServiceTokenParser {
    String parse(String jwt) throws ServiceTokenParsingException;
}

