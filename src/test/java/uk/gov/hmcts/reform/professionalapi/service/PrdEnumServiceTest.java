package uk.gov.hmcts.reform.professionalapi.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;

public class PrdEnumServiceTest {

    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);

    private final PrdEnumServiceImpl prdEnumService = new PrdEnumServiceImpl(prdEnumRepository);


    @Test
    public void retrieves_prd_enums_correctly() {

        List<PrdEnum> prdEnums = prdEnumService.findAllPrdEnums();

        verify(
                prdEnumRepository,
                times(1)).findAll();
    }

}
