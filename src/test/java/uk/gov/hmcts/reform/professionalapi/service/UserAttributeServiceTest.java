package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;

@Ignore
public class UserAttributeServiceTest {

    private final UserAttributeRepository userAttributeRepository = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);
    private final PrdEnumServiceImpl prdEnumService = mock(PrdEnumServiceImpl.class);
    private final UserAttributeServiceImpl userAttributeService = new UserAttributeServiceImpl(userAttributeRepository, prdEnumRepository, prdEnumService);

    private final PrdEnum prdEnum = mock(PrdEnum.class);
    private final PrdEnumId prdEnumId = mock(PrdEnumId.class);


    private final ProfessionalUser professionalUser = mock(ProfessionalUser.class);
    private List<String> userRoles = new ArrayList<>();
    private List<PrdEnum> prdEnums = new ArrayList<>();

    @Before
    public void setUp() {
        PrdEnum anEnum = new PrdEnum(prdEnumId, "PUI_USER_MANAGER", "USER_ROLE");

        prdEnums.add(anEnum);

        userRoles.add("pui-user-manager");
    }

    @Ignore
    @Test
    public void adds_user_attributes_to_user_correctly() {
        when(prdEnumService.findAllPrdEnums()).thenReturn(prdEnums);

        userAttributeService.addUserAttributesToUser(professionalUser, userRoles);

        assertThat(professionalUser.getUserAttributes()).isNotNull();

        verify(
                prdEnumService,
                times(1)).findAllPrdEnums();
        verify(
                userAttributeRepository,
                times(1)).save(any(UserAttribute.class));
    }

    @Test(expected = InvalidRequest.class)
    public void addUserToOrganisationWithInvalidValues() {
        List<String> emptyUserRoles = new ArrayList<>();

        userAttributeService.addUserAttributesToUser(professionalUser, emptyUserRoles);
    }
}