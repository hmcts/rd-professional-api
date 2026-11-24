package uk.gov.hmcts.reform.professionalapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
class PrdEnumServiceImplTest {

    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);
    private final PrdEnumId prdEnumId = new PrdEnumId(1, "SIDAM_ROLE");
    private final PrdEnumId prdEnumId2 = new PrdEnumId(5, "CCD_ROLE");
    private final PrdEnumId prdEnumId3 = new PrdEnumId(4, "ADMIN_ROLE");
    private final List<PrdEnum> prdEnums = new ArrayList<>();
    private PrdEnumServiceImpl prdEnumService;


    @BeforeEach
    void setUp() {
        prdEnumService = new PrdEnumServiceImpl(prdEnumRepository);
        PrdEnum anEnum = new PrdEnum(prdEnumId, "PUI_USER_MANAGER", "SIDAM_ROLE");
        PrdEnum anEnumTwo2 = new PrdEnum(prdEnumId2, "caseworker", "CCD_ROLE");
        prdEnums.add(anEnum);
        prdEnums.add(anEnumTwo2);
    }

    @Test
    void test_gets_user_roles_of_user_correctly_other_than_role_type() {
        when(prdEnumRepository.findAll()).thenReturn(prdEnums);
        when(prdEnumService.findAllPrdEnums()).thenReturn(prdEnums);
        List<String> roleList = prdEnumService.getPrdEnumByEnumType("ADMIN_ROLE");
        assertThat(roleList).hasSize(2);
        assertThat(roleList.get(0)).isEqualTo("PUI_USER_MANAGER");
        assertThat(roleList.get(1)).isEqualTo("caseworker");

        verify(prdEnumRepository, times(1)).findByEnabled("YES");
    }

    @Test
    void test_gets_no_user_roles_of_user_by_admin_role_type() {

        List<PrdEnum> prdEnums = new ArrayList<>();
        PrdEnum anEnum = new PrdEnum(prdEnumId3, "PRD-ADMIN", "ADMIN_ROLE");
        prdEnums.add(anEnum);

        when(prdEnumRepository.findAll()).thenReturn(prdEnums);
        when(prdEnumService.findAllPrdEnums()).thenReturn(prdEnums);
        List<String> roleList = prdEnumService.getPrdEnumByEnumType("ADMIN_ROLE");
        assertThat(roleList).isEmpty();
        verify(prdEnumRepository, times(1)).findByEnabled("YES");
    }
}