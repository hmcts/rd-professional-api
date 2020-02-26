package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;

public class PrdEnumServiceTest {

    private final PrdEnumRepository prdEnumRepositoryMock = mock(PrdEnumRepository.class);

    private final PrdEnumServiceImpl prdEnumService = new PrdEnumServiceImpl(prdEnumRepositoryMock);


    @Test
    public void testRetrievesPrdEnumsCorrectly() {
        List<PrdEnum> prdEnums = prdEnumService.findAllPrdEnums();

        verify(prdEnumRepositoryMock, times(1)).findByEnabled("YES");
        assertThat(prdEnums).isNotNull();
    }
}
