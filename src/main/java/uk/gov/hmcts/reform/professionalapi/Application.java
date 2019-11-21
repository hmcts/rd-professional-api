package uk.gov.hmcts.reform.professionalapi;

import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.azure.storage.blob.TransferManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.AsynchronousFileChannel;
import java.security.InvalidKeyException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import lombok.extern.slf4j.Slf4j;
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


    private static final String BLOB_URL = "http://rd.demo.platform.hmcts.net";
    private static final String BLOB_HTTPS_URL = "https://rd.demo.platform.hmcts.net";
    private static final boolean isEnableHttps = false;
    private static final String AZURE_ACCOUNT_NAME = "rddemo";
    private static final String AZURE_ACCOUNT_KEY = "Vjmr3Yk0DUrygumP7FKJ2eUiOyhWvZ5XczDMk13J+m9vtk2IOXEcOlfjlcTaHDb5mHwAsq7qOYf3GtxEo/uFIA==";
    private static final String CONTAINER_NAME = "jrdtest";

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public void run(String... var1) throws IOException, InvalidKeyException {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    pushFile();
                } catch (IOException e) {
                    log.error("IOException", e);
                } catch (InvalidKeyException e) {
                    log.error("InvalidKeyException", e);
                }
            }
        }, 0, 60 * 1000 * 10);
    }

    public void pushFile() throws IOException, InvalidKeyException {

        final String sourceCsvFile = "content_demo.csv";
        final File sourceFile = new File(this.getClass().getClassLoader().getResource(sourceCsvFile).getFile());
        ServiceURL serviceUrl = createServiceUrl(new PipelineOptions());
        ContainerURL containerUrl = serviceUrl.createContainerURL(CONTAINER_NAME);
        final BlockBlobURL blockBlobUrl = containerUrl.createBlockBlobURL(sourceCsvFile);
        uploadFile(blockBlobUrl, sourceFile);
    }

    public static void uploadFile(BlockBlobURL blob, File sourceFile) throws IOException {
        log.info("Start uploading file " + sourceFile.getName());
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

    public static String createFile() {
        String fileName;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        fileName = LocalTime.now().format(dtf);

        try {
            File file = new File(fileName + ".csv");
            FileWriter fw = new FileWriter(file);
            fw.write("A,B,C,D");
            fw.flush();
            fw.close();
        } catch (FileNotFoundException e) {
            log.error("File not found", e);
        } catch (IOException e) {
            log.error("IOException found", e);
        }
        return fileName;
    }
}
