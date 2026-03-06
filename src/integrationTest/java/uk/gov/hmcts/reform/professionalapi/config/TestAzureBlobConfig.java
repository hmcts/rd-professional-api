package uk.gov.hmcts.reform.professionalapi.config;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.dataload.config.AzureBlobConfig;

@TestConfiguration
public class TestAzureBlobConfig {

    private static final String DUMMY_ACCOUNT_NAME = "test-storage";
    private static final String DUMMY_ACCOUNT_KEY_BASE64 = "dGVzdA=="; // Base64 for "test"
    private static final String DUMMY_CONTAINER_NAME = "test-container";
    private static final String DUMMY_BLOB_URL_SUFFIX = "core.windows.net";

    @Bean
    @Primary
    public AzureBlobConfig azureBlobConfig() {
        AzureBlobConfig config = new AzureBlobConfig();
        config.setAccountName(DUMMY_ACCOUNT_NAME);
        config.setAccountKey(DUMMY_ACCOUNT_KEY_BASE64);
        config.setContainerName(DUMMY_CONTAINER_NAME);
        config.setBlobUrlSuffix(DUMMY_BLOB_URL_SUFFIX);
        return config;
    }

    @Bean(name = "credsreg")
    @Primary
    public StorageCredentials storageCredentials(AzureBlobConfig azureBlobConfig) {
        return new StorageCredentialsAccountAndKey(
                azureBlobConfig.getAccountName(),
                azureBlobConfig.getAccountKey()
        );
    }

    @Bean(name = "credscloudStorageAccount")
    @Primary
    public CloudStorageAccount cloudStorageAccount(
            @Qualifier("credsreg") StorageCredentials storageCredentials
    ) throws Exception {
        return new CloudStorageAccount(storageCredentials, true);
    }
}
