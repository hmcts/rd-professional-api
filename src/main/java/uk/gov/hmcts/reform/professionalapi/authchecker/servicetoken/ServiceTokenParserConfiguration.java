package uk.gov.hmcts.reform.professionalapi.authchecker.servicetoken;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;


@Configuration
@Lazy
public class ServiceTokenParserConfiguration {

    @Bean(name = "serviceTokenParserHttpClient")
    @ConditionalOnProperty(
            value = "ssl.verification.enable",
            havingValue = "false",
            matchIfMissing = true)
    public HttpClient serviceTokenParserHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

            HttpClientBuilder httpClientBuilder = HttpClients.custom()
                    .disableCookieManagement()
                    .disableAuthCaching()
                    .useSystemProperties();

            TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;
            // ignore Sonar's weak hostname verifier as we are deliberately disabling SSL verification
            HostnameVerifier allowAllHostnameVerifier = (hostName, session) -> true; // NOSONAR
            SSLContext sslContextWithoutValidation = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();

            SSLConnectionSocketFactory allowAllSslSocketFactory = new SSLConnectionSocketFactory(
                    sslContextWithoutValidation,
                    allowAllHostnameVerifier);

            httpClientBuilder.setSSLSocketFactory(allowAllSslSocketFactory);

            // also disable SSL validation for plain java http url connection
            HttpsURLConnection.setDefaultHostnameVerifier(allowAllHostnameVerifier);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContextWithoutValidation.getSocketFactory());

            CloseableHttpClient client = httpClientBuilder.build();
        return client;
    }

    @Bean
    public ServiceTokenParser serviceAuthProviderAuthCheckClient(HttpClient serviceTokenParserHttpClient,
                                                                 @Value("${auth.provider.service.client.baseUrl}") String baseUrl) {

        return new HttpComponentsBasedServiceTokenParser(serviceTokenParserHttpClient, baseUrl);
    }

}

