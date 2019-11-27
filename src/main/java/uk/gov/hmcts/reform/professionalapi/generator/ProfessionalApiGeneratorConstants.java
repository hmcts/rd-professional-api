package uk.gov.hmcts.reform.professionalapi.generator;

public class ProfessionalApiGeneratorConstants {

    private ProfessionalApiGeneratorConstants() {
    }

    public static final int LENGTH_OF_UUID = 36;
    public static final int LENGTH_OF_ORGANISATION_IDENTIFIER = 7;
    public static final String ORGANISATION_IDENTIFIER_FORMAT_REGEX = "^(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]{7}$";

}
