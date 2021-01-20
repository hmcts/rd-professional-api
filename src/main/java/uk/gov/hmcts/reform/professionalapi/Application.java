package uk.gov.hmcts.reform.professionalapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@SpringBootApplication
@EnableCaching
@EnableFeignClients(basePackages = {
        "uk.gov.hmcts.reform.professionalapi" },
        basePackageClasses = { IdamApi.class, ServiceAuthorisationApi.class }
        )
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application  {
    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
