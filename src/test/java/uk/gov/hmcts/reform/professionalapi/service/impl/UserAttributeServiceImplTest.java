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

import org.mockito.Mockito;
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
    private final UserAttributeServiceImpl userAttributeServiceMock
            = new UserAttributeServiceImpl(userAttributeRepositoryMock, prdEnumRepositoryMock, prdEnumServiceMock);

    private final PrdEnumId prdEnumIdMock = new PrdEnumId(1, "JURISD_ID");
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

        userAttributeServiceMock.addUserAttributesToUser(professionalUser, userRoles, prdEnums);

        assertThat(professionalUser.getUserAttributes()).isNotNull();
        verify(userAttributeRepositoryMock, times(1)).saveAll(any());
    }

    @Test
    public void test_AddAllAttributes() {
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"), "PROBATE", "PROBATE"));

        when(userAttributeRepositoryMock.saveAll(any())).thenReturn(userAttributes);

        int expectSize = userAttributes.size() + 1;

        ProfessionalUser professionalUserMock = Mockito.mock(ProfessionalUser.class);
        List<String> jurisdictionIds = new ArrayList<>();
        jurisdictionIds.add("PROBATE");

        List<UserAttribute> result = userAttributeServiceMock
                .addUserAttributesToSuperUserWithJurisdictions(professionalUserMock, userAttributes, jurisdictionIds);

        assertThat(result.size()).isEqualTo(expectSize);

        verify(userAttributeRepositoryMock, times(1)).saveAll(any());
    }

    @Test
    public void test_validEnumType() {
        List<String> jurisdictionIds = new ArrayList<>();
        jurisdictionIds.add("PROBATE");
        PrdEnum prdEnum = new PrdEnum(new PrdEnumId(10, "JURISD_ID"), "PROBATE",
                "PROBATE");
        boolean flag = userAttributeServiceMock.isValidEnumType("SIDAM_ROLE", jurisdictionIds,  prdEnum);

        assertThat(flag).isTrue();

        boolean flag1 = userAttributeServiceMock.isValidEnumType("ADMIN_ROLE", jurisdictionIds,  prdEnum);

        assertThat(flag1).isTrue();

        boolean flag2 = userAttributeServiceMock.isValidEnumType("JURISD_ID_1", jurisdictionIds,  prdEnum);

        assertThat(flag2).isFalse();

    }
}