package uk.gov.hmcts.reform.professionalapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@EnableJpaAuditing
@EnableJpaRepositories
@EnableRetry
@SpringBootApplication
@EnableCaching
@EnableCircuitBreaker
@EnableFeignClients(basePackages = {
        "uk.gov.hmcts.reform.professionalapi",
})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application  {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
