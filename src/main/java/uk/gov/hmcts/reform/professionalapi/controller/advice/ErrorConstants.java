package uk.gov.hmcts.reform.professionalapi.controller.advice;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings({
        "checkstyle:HideUtilityClassConstructor",
        "PMD.FieldNamingConventions"
})
public class ErrorConstants {

    public static final String MALFORMED_JSON =  "Malformed Input Request";

    public static final String UNSUPPORTED_MEDIA_TYPES = "Unsupported Media Types";

    public static final String INVALID_REQUEST =  "There is a problem with your request. Please check and try again";

    public static final String EMPTY_RESULT_DATA_ACCESS = "Result was expected to have at least one row (or element) but zero rows (or elements) were actually returned.";

    public static final String METHOD_ARG_NOT_VALID = "validation on an argument annotated with @Valid fails";

    public static final String DATA_INTEGRITY_VIOLATION = "attempt to insert or update data resulted in violation of an integrity constraint";

    public static final String ILLEGAL_ARGUMENT = "method has been passed an illegal or inappropriate argument";
}