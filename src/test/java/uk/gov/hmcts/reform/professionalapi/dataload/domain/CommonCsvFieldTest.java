package uk.gov.hmcts.reform.professionalapi.dataload.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonCsvFieldTest {


    @Test
    void test_CommonCsvField() {
        CommonCsvField commonCsvField = new CommonCsvField();
        commonCsvField.setRowId(1L);
        assertThat(commonCsvField.getRowId()).isEqualTo(1L);

    }

}
