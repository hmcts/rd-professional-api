package uk.gov.hmcts.reform.professionalapi.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;

public class PrdEnumTest {

    @Test
    public void creates_prd_enum_correctly() {

        PrdEnumId prdEnumId = mock(PrdEnumId.class);

        PrdEnum prdEnum = new PrdEnum(prdEnumId, "enum-name", "enum-desc");

        assertThat(prdEnum.getPrdEnumId()).isEqualTo(prdEnumId);
        assertThat(prdEnum.getEnumName()).isEqualTo("enum-name");
        assertThat(prdEnum.getEnumDescription()).isEqualTo("enum-desc");
    }
}
