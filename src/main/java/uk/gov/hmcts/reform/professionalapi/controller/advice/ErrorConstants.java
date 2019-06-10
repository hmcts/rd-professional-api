package uk.gov.hmcts.reform.professionalapi.controller.advice;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings({
        "checkstyle:HideUtilityClassConstructor",
        "PMD.FieldNamingConventions"
})
public class ErrorConstants {

    public static final String MALFORMED_JSON =  "Malformed Input Request";

    public static final String UNSUPPORTED_MEDIA_TYPES = "Unsupported Media Type";

    public static final String INVALID_REQUEST =  "There is a problem with your request. Please check and try again";

    public static final String EMPTY_RESULT_DATA_ACCESS = "Resource not found";

    public static final String METHOD_ARG_NOT_VALID = "validation on an argument failed";

    public static final String DATA_INTEGRITY_VIOLATION = "attempt to insert or update data resulted in violation of an integrity constraint";

    public static final String ILLEGAL_ARGUMENT = "method has been passed an illegal or inappropriate argument";
}