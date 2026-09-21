package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.annotations.WithTags;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.Application;
import uk.gov.hmcts.reform.professionalapi.wiremock.WireMockContextInitializer;

@ExtendWith({SpringExtension.class, SerenityJUnit5Extension.class})
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = WireMockContextInitializer.class)
@WithTags({@WithTag("testType:Integration")})
public abstract class SpringBootIntegrationTest {

    @LocalServerPort
    protected int port;

    @MockitoBean
    @Qualifier("credsreg")
    StorageCredentials storageCredentials;

    @MockitoBean
    @Qualifier("credscloudStorageAccount")
    CloudStorageAccount cloudStorageAccount;

    @Autowired
    protected TestApplicationServer testApplicationServer;

    public static ObjectMapper getObjectMapper() {

        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;

    }

}
