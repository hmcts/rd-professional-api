package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;

public class PrdEnumServiceImplTest {

    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);
    private final PrdEnumId prdEnumId = new PrdEnumId(1, "SIDAM_ROLES");


    private List<String> userRoles = new ArrayList<>();
    private List<PrdEnum> prdEnums = new ArrayList<>();

    @Before
    public void setUp() {
        PrdEnum anEnum = new PrdEnum(prdEnumId, "PUI_USER_MANAGER", "SIDAM_ROLES");
        prdEnums.add(anEnum);
    }

    @Test
    public void gets_user_roles_of_user_correctly_by_role_type() {

        PrdEnumServiceImpl prdEnumService = new PrdEnumServiceImpl(prdEnumRepository);
        when(prdEnumRepository.findAll()).thenReturn(prdEnums);
        when(prdEnumService.findAllPrdEnums()).thenReturn(prdEnums);
        List roleList = prdEnumService.getPrdEnumByEnumType("SIDAM_ROLES");
        assertThat(roleList.size()).isEqualTo(1);
        verify(
                prdEnumRepository,
                times(1)).findAll();
    }
}