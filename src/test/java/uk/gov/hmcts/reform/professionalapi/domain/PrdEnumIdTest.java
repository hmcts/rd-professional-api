package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;

@ExtendWith(MockitoExtension.class)
class PrdEnumIdTest {

    @Test
    void test_creates_prd_enum_id_correctly() {
        PrdEnumId prdEnumId = new PrdEnumId(1, "enum-type");

        assertEquals(1, prdEnumId.getEnumCode());
        assertThat(prdEnumId.getEnumType()).isEqualTo("enum-type");
    }

    @Test
    void test_NoArgsConstructor() {
        PrdEnumId prdEnumId = new PrdEnumId();
        assertThat(prdEnumId).isNotNull();
    }
}
