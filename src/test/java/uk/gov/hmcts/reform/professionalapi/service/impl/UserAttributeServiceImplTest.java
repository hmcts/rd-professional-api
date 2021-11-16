package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;

class UserAttributeServiceImplTest {

    private final UserAttributeRepository userAttributeRepositoryMock = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepositoryMock = mock(PrdEnumRepository.class);
    private final PrdEnumServiceImpl prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
    private final UserAttributeServiceImpl userAttributeServiceMock
            = new UserAttributeServiceImpl(userAttributeRepositoryMock, prdEnumRepositoryMock, prdEnumServiceMock);

    private final PrdEnumId prdEnumIdMock = new PrdEnumId(1, "PRD_ROLE");
    private final Organisation organisation = new Organisation("some-org-name", null, "PENDING",
            null, null, null);
    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname", "some@hmcts.net", organisation);
    private List<String> userRoles = new ArrayList<>();
    private List<PrdEnum> prdEnums = new ArrayList<>();
    private PrdEnum anEnum;
    private UserAttribute userAttribute = new UserAttribute(professionalUser, anEnum);
    private List<UserAttribute> userAttributes =  new ArrayList<>();

    @BeforeEach
    void setUp() {
        anEnum = new PrdEnum(prdEnumIdMock, "pui-user-manager", "SIDAM_ROLE");
        userAttributes.add(userAttribute);
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);
        prdEnums.add(anEnum);
        userRoles.add("pui-user-manager");
    }

    @Test
    void test_adds_user_attributes_to_user_correctly() {
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);

        userAttributeServiceMock.addUserAttributesToUser(professionalUser, userRoles, prdEnums);

        assertThat(professionalUser.getUserAttributes()).isNotNull();
        verify(userAttributeRepositoryMock, times(1)).saveAll(any());
    }
}