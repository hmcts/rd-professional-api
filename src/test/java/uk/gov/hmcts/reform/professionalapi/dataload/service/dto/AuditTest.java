package uk.gov.hmcts.reform.professionalapi.dataload.service.dto;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class AuditTest {


    @Test
    void test_Audit() {

        Audit audit = new Audit();
        audit.setFileName("filename");
        audit.setStatus("status");
        Date utilDate = getDate(2021, 11, 16, 10, 0, 0);
        audit.setSchedulerStartTime(utilDate);
        assertThat(audit.getFileName()).isNotEmpty();
        assertThat(audit.getStatus()).isNotEmpty();
        assertThat(audit.getSchedulerStartTime())
            .isEqualTo(getDate(2021, 11, 16, 10, 0, 0));
    }

    @Test
    void test_AuditWitConstructor() {

        Audit audit = new Audit("filename",
            getDate(2021, 11, 16, 10, 0, 0),"status");
        assertThat(audit.getFileName()).isNotEmpty();
        assertThat(audit.getStatus()).isNotEmpty();
        assertThat(audit.getSchedulerStartTime())
            .isEqualTo(getDate(2021, 11, 16, 10, 0, 0));
    }


    private static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

}
