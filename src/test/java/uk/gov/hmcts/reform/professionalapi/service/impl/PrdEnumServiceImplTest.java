package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;

public class PrdEnumServiceImplTest {

    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);
    private final PrdEnumService prdEnumServiceMock = mock(PrdEnumService.class);
    private final PrdEnumId prdEnumId = new PrdEnumId(1, "SIDAM_ROLE");
    private final PrdEnumId prdEnumId2 = new PrdEnumId(5, "CCD_ROLE");
    private final PrdEnumId prdEnumId3 = new PrdEnumId(4, "ADMIN_ROLE");
    private List<String> userRoles = new ArrayList<>();
    private List<PrdEnum> prdEnums = new ArrayList<>();

    @Before
    public void setUp() {
        PrdEnum anEnum = new PrdEnum(prdEnumId, "PUI_USER_MANAGER", "SIDAM_ROLE");
        PrdEnum anEnumTwo2 = new PrdEnum(prdEnumId2, "caseworker", "CCD_ROLE");
        prdEnums.add(anEnum);
        prdEnums.add(anEnumTwo2);
    }

    @Test
    public void gets_user_roles_of_user_correctly_other_than_role_type() {

        PrdEnumServiceImpl prdEnumService = new PrdEnumServiceImpl(prdEnumRepository);
        when(prdEnumRepository.findAll()).thenReturn(prdEnums);
        when(prdEnumService.findAllPrdEnums()).thenReturn(prdEnums);
        List roleList = prdEnumService.getPrdEnumByEnumType("ADMIN_ROLE");
        assertThat(roleList.size()).isEqualTo(2);
    }

    @Test
    public void gets_no_user_roles_of_user_by_admin_role_type() {

        List<PrdEnum> prdEnums = new ArrayList<>();
        PrdEnum anEnum = new PrdEnum(prdEnumId3, "PRD-ADMIN", "ADMIN_ROLE");
        prdEnums.add(anEnum);

        PrdEnumServiceImpl prdEnumService = new PrdEnumServiceImpl(prdEnumRepository);
        when(prdEnumRepository.findAll()).thenReturn(prdEnums);
        when(prdEnumService.findAllPrdEnums()).thenReturn(prdEnums);
        List roleList = prdEnumService.getPrdEnumByEnumType("ADMIN_ROLE");
        assertThat(roleList.size()).isEqualTo(0);
    }
}