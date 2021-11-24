package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;

public class UserAttributeServiceImplTest {

    private final UserAttributeRepository userAttributeRepositoryMock = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepositoryMock = mock(PrdEnumRepository.class);
    private final PrdEnumServiceImpl prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
    private final UserAttributeServiceImpl sut
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

    @Before
    public void setUp() {
        anEnum = new PrdEnum(prdEnumIdMock, "pui-user-manager", "SIDAM_ROLE");
        userAttributes.add(userAttribute);
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);
        prdEnums.add(anEnum);
        userRoles.add("pui-user-manager");
    }

    @Test
    public void test_adds_user_attributes_to_user_correctly() {
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);

        sut.addUserAttributesToUser(professionalUser, userRoles, prdEnums);

        assertThat(professionalUser.getUserAttributes()).isNotNull();
        verify(userAttributeRepositoryMock, times(1)).saveAll(any());
    }

    @Test
    public void test_adds_super_user_attributes_to_user_correctly() {
        List<UserAttribute> userAttributes = new ArrayList<>();
        userAttributes.add(new UserAttribute(professionalUser, prdEnums.get(0)));

        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);

        List<UserAttribute> userAttributeResponse = sut.addUserAttributesToSuperUser(professionalUser, userAttributes);

        assertThat(userAttributeResponse).isNotNull();
        assertThat(professionalUser.getUserAttributes()).isNotNull();
        verify(userAttributeRepositoryMock, times(1)).saveAll(any());
    }

    @Test
    public void test_isValidEnumType() {
        boolean response = sut.isValidEnumType("SIDAM_ROLE");
        assertThat(response).isTrue();

        boolean response1 = sut.isValidEnumType("ADMIN_ROLE");
        assertThat(response1).isTrue();

        boolean response2 = sut.isValidEnumType("INVALID_ROLE");
        assertThat(response2).isFalse();
    }
}