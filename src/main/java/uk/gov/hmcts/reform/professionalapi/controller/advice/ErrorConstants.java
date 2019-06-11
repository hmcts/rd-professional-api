package uk.gov.hmcts.reform.professionalapi.controller.advice;


@SuppressWarnings({
        "checkstyle:HideUtilityClassConstructor",
        "PMD.FieldNamingConventions"
})
public interface ErrorConstants {

    static String MALFORMED_JSON =  "Malformed Input Request";

    static String UNSUPPORTED_MEDIA_TYPES = "Unsupported Media Type";

    static String INVALID_REQUEST =  "There is a problem with your request. Please check and try again";

    static String EMPTY_RESULT_DATA_ACCESS = "Resource not found";

    static String METHOD_ARG_NOT_VALID = "validation on an argument failed";

    static String DATA_INTEGRITY_VIOLATION = "attempt to insert or update data resulted in violation of an integrity constraint";

    static String ILLEGAL_ARGUMENT = "method has been passed an illegal or inappropriate argument";

    static String UNKNOWN_EXCEPTION = " error was caused by an unknown exception";
}