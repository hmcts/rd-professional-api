package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PrdDataloadSchedulerJobTest {

    @Test
    void test_pbaResponseNoArgsConstructor() {
        PrdDataloadSchedulerJob audit = new PrdDataloadSchedulerJob();
        LocalDateTime jobStartTime = now();

        audit.setJobStartTime(jobStartTime);
        audit.setId(1);
        LocalDateTime jobEndTime = now();
        audit.setJobEndTime(jobEndTime);
        audit.setPublishingStatus("SUCCESS");

        assertThat(audit.getId()).isEqualTo(1);
        assertThat(audit.getPublishingStatus()).isEqualTo("SUCCESS");
        assertThat(audit.getJobEndTime()).isEqualTo(jobEndTime);
        assertThat(audit.getJobStartTime()).isEqualTo(jobStartTime);
    }
}
