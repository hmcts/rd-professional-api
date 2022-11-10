package uk.gov.hmcts.reform.professionalapi.util;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class ProfessionalApiPostgresqlContainer extends PostgreSQLContainer<ProfessionalApiPostgresqlContainer> {
    private static final String IMAGE_VERSION = "postgres:11.1";

    private ProfessionalApiPostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    @Container
    private static final ProfessionalApiPostgresqlContainer container = new ProfessionalApiPostgresqlContainer();

}