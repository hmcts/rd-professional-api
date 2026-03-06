package uk.gov.hmcts.reform.professionalapi.util;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.Application;
import uk.gov.hmcts.reform.professionalapi.config.TestAzureBlobConfig;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestAzureBlobConfig.class)
public abstract class SpringBootIntegrationTest {

    @LocalServerPort
    protected int port;

}
