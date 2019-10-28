package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;

public class PrdEnumIdTest {

    @Test
    public void creates_prd_enum_id_correctly() {

        PrdEnumId prdEnumId = new PrdEnumId(1, "enum-type");

        assertEquals(1, prdEnumId.getEnumCode());
        assertThat(prdEnumId.getEnumType()).isEqualTo("enum-type");
    }

    @Test
    public void test_NoArgsConstructor() {
        PrdEnumId prdEnumId = new PrdEnumId();
        assertThat(prdEnumId).isNotNull();
    }
}
