package uk.gov.hmcts.reform.professionalapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PrdEnumTest {

    @Test
    void creates_prd_enum_correctly() {
        PrdEnumId prdEnumId = new PrdEnumId();

        PrdEnum prdEnum = new PrdEnum(prdEnumId, "enum-name", "enum-desc");

        assertThat(prdEnum.getPrdEnumId()).isEqualTo(prdEnumId);
        assertThat(prdEnum.getEnumName()).isEqualTo("enum-name");
        assertThat(prdEnum.getEnumDescription()).isEqualTo("enum-desc");
    }

    @Test
    void test_NoArgsConstructor() {
        PrdEnum prdEnum = new PrdEnum();
        assertThat(prdEnum).isNotNull();
    }
}
