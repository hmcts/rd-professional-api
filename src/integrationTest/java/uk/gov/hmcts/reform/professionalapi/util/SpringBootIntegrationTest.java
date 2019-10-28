package uk.gov.hmcts.reform.professionalapi.util;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.ConsumerApplication;
//import uk.gov.hmcts.reform.professionalapi.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SpringBootIntegrationTest {

    @LocalServerPort
    protected int port;

}
