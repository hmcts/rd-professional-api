package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;

class PrdEnumServiceTest {

    private final PrdEnumRepository prdEnumRepositoryMock = mock(PrdEnumRepository.class);

    private final PrdEnumServiceImpl prdEnumService = new PrdEnumServiceImpl(prdEnumRepositoryMock);


    @Test
    void test_RetrievesPrdEnumsCorrectly() {
        List<PrdEnum> prdEnums = prdEnumService.findAllPrdEnums();

        verify(prdEnumRepositoryMock, times(1)).findByEnabled("YES");
        assertThat(prdEnums).isNotNull();
    }
}
