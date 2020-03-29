package uk.gov.hmcts.reform.professionalapi.authchecker.servicetoken;

import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.ServiceTokenParsingException;

public interface ServiceTokenParser {
    String parse(String jwt) throws ServiceTokenParsingException;
}

