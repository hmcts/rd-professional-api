package uk.gov.hmcts.reform.professionalapi;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.azure.storage.blob.TransferManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.security.InvalidKeyException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BouncyGPG;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.callbacks.KeyringConfigCallbacks;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfig;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfigs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;


@Slf4j
@EnableJpaAuditing
@EnableJpaRepositories
@EnableRetry
@SpringBootApplication
@EnableCircuitBreaker
@EnableFeignClients(basePackages = {
        "uk.gov.hmcts.reform.professionalapi",
})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application  implements CommandLineRunner {

    //private static final String BLOB_URL = "http://rd.demo.platform.hmcts.net";
    private static final String BLOB_URL = "http://rddemo.blob.core.windows.net";
    private static final String BLOB_HTTPS_URL = "https://rd.demo.platform.hmcts.net";
    private static final boolean isEnableHttps = false;
    private static final String AZURE_ACCOUNT_NAME = "rddemo";
    private static final String AZURE_ACCOUNT_KEY = "Vjmr3Yk0DUrygumP7FKJ2eUiOyhWvZ5XczDMk13J+m9vtk2IOXEcOlfjlcTaHDb5mHwAsq7qOYf3GtxEo/uFIA==";
    private static final String CONTAINER_NAME = "jrdtest";

    private static final String SFTP_USER = "kiren.bhardwa@hmcts.net";
    private static final String SFTP_USER_PASSWORD = "frame-n9Lf)xiFUz";
    private static final String SFTP_HOST = "91.186.188.7";//https://files2.ciphr247.com";
    private static final String SFTP_FILE_NAME = "Courts.csv.gpg";
    private static final Integer SFTP_TIME_OUT = 60000;
    private static final String GPG_PASSWORD = "Shreedhar!9";

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(String... var1) throws IOException, InvalidKeyException {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    File sftpEncryptedFile = getFileFromSftp();
                    File decryptedSftpFile = decryptFile(sftpEncryptedFile);
                    pushFile(decryptedSftpFile);
                } catch (IOException e) {
                    log.error("IOException", e);
                } catch (InvalidKeyException e) {
                    log.error("InvalidKeyException", e);
                }  catch (Exception e) {
                    log.error("Exception", e);
                }
            }
        }, 0, 10 * 1000 * 360);
    }

    public void pushFile(File sftpFile) throws IOException, InvalidKeyException, URISyntaxException {

        ServiceURL serviceUrl = createServiceUrl(new PipelineOptions());
        ContainerURL containerUrl = serviceUrl.createContainerURL(CONTAINER_NAME);
        final BlockBlobURL blockBlobUrl = containerUrl.createBlockBlobURL(sftpFile.getName());
        uploadFile(blockBlobUrl, sftpFile);
    }

    public static void uploadFile(BlockBlobURL blob, File sourceFile) throws IOException {

        final AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(sourceFile.toPath());

        TransferManager.uploadFileToBlockBlob(fileChannel, blob, 8 * 1024 * 1024, null)
                .ignoreElement()
                .doOnComplete(() -> log.info("File " + sourceFile.getName() + " is uploaded."))
                .doOnError(error -> log.error("Failed to upload file " + sourceFile.getName() + " with error %s.", sourceFile.toPath(),
                        error.getMessage()))
                .blockingAwait();
    }

    public static ServiceURL createServiceUrl(@Autowired(required = false) PipelineOptions options) throws InvalidKeyException,
            MalformedURLException {
        log.info("Creating ServiceURL bean...");
        final SharedKeyCredentials credentials = new SharedKeyCredentials(AZURE_ACCOUNT_NAME,
                AZURE_ACCOUNT_KEY);
        final URL blobUrl = getUrl();
        final ServiceURL serviceUrl = new ServiceURL(blobUrl, StorageURL.createPipeline(credentials, new PipelineOptions()));

        return serviceUrl;
    }

    private static URL getUrl() throws MalformedURLException {
        if (isEnableHttps) {
            return new URL(String.format(BLOB_HTTPS_URL, AZURE_ACCOUNT_NAME));
        }
        return new URL(String.format(BLOB_URL, AZURE_ACCOUNT_NAME));
    }

    public File getFileFromSftp() throws IOException, URISyntaxException {

        FileSystemManager manager = VFS.getManager();
        FileSystemOptions opts = new FileSystemOptions();
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
        SftpFileSystemConfigBuilder.getInstance().setConnectTimeoutMillis(opts, SFTP_TIME_OUT);
        SftpFileSystemConfigBuilder.getInstance().setSessionTimeoutMillis(opts, SFTP_TIME_OUT);

        String userInfo = SFTP_USER + ":" + SFTP_USER_PASSWORD;
        String path = "/" + SFTP_FILE_NAME;
        URI sftpUri = new URI("sftp", userInfo, SFTP_HOST, 22, path, null, null);

        FileObject remoteFile = manager.resolveFile(sftpUri.toString(),opts);
        remoteFile.createFile();
        FileContent fileContent = remoteFile.getContent();
        InputStream inputStream = fileContent.getInputStream();
        File localFile = createLocalFileForSftp(inputStream);
        return localFile;

    }

    public File createLocalFileForSftp(InputStream inputStream) throws IOException {

        String fileName;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");//yyyy-MM-dd HH:mm:ss
        fileName = LocalTime.now().format(dtf);
        File sftpFile = File.createTempFile(fileName, ".csv");
        FileUtils.copyInputStreamToFile(inputStream, sftpFile);
        return sftpFile;
    }


    public File decryptFile(File sftpEncryptedFile) throws IOException, NoSuchProviderException {
        KeyringConfig keyringConfig = keyringConfigInMemoryForKeys(GPG_PASSWORD);

        final FileInputStream cipherTextStream = new FileInputStream(sftpEncryptedFile);
        Security.addProvider(new BouncyCastleProvider());
        InputStream plaintextStream;
        plaintextStream = BouncyGPG
                .decryptAndVerifyStream()
                .withConfig(keyringConfig)
                .andIgnoreSignatures()
                  .fromEncryptedInputStream(cipherTextStream);

        return createLocalFileForSftp(plaintextStream);
    }


    public KeyringConfig keyringConfigInMemoryForKeys(final String passphrase) throws IOException {

        final File publicKeyFile = new File(this.getClass().getClassLoader().getResource("my_public_key.gpg").getFile());
        final File privateKeyFile = new File(this.getClass().getClassLoader().getResource("my_private_key.gpg").getFile());

        final KeyringConfig keyringConfig = KeyringConfigs
                .withKeyRingsFromFiles(
                        publicKeyFile,
                        privateKeyFile,
                        KeyringConfigCallbacks.withPassword(passphrase));

        return keyringConfig;
    }

}
