package uk.gov.hmcts.reform.professionalapi.util;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.Application;
import uk.gov.hmcts.reform.professionalapi.repository.TestConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
public abstract class SpringBootIntegrationTest {

    @LocalServerPort
    protected int port;

}
