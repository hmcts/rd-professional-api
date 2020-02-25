package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PrdEnumTest {

    @Test
    public void creates_prd_enum_correctly() {
        PrdEnumId prdEnumId = new PrdEnumId();

        PrdEnum prdEnum = new PrdEnum(prdEnumId, "enum-name", "enum-desc");

        assertThat(prdEnum.getPrdEnumId()).isEqualTo(prdEnumId);
        assertThat(prdEnum.getEnumName()).isEqualTo("enum-name");
        assertThat(prdEnum.getEnumDescription()).isEqualTo("enum-desc");
    }

    @Test
    public void test_NoArgsConstructor() {
        PrdEnum prdEnum = new PrdEnum();
        assertThat(prdEnum).isNotNull();
    }
}
