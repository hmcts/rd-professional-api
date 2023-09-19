package uk.gov.hmcts.reform.professionalapi.dataload.config;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsAccountAndKey;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlobStorageCredentials {

    @Autowired
    AzureBlobConfig azureBlobConfig;

    @Autowired
    @Qualifier("credsreg")
    StorageCredentials storageCredentials;

    @Bean(name = "credsreg")
    public StorageCredentials credentials() {
        return new StorageCredentialsAccountAndKey(azureBlobConfig.getAccountName(), azureBlobConfig.getAccountKey());
    }

    @Bean(name = "credscloudStorageAccount")
    public CloudStorageAccount cloudStorageAccount() throws Exception {
        return new CloudStorageAccount(storageCredentials,
            true);
    }

}