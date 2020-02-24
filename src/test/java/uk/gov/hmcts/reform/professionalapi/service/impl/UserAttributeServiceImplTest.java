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
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;

public class UserAttributeServiceImplTest {

    private final UserAttributeRepository userAttributeRepositoryMock = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepositoryMock = mock(PrdEnumRepository.class);
    private final PrdEnumServiceImpl prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
    private final UserAttributeServiceImpl userAttributeServiceMock = new UserAttributeServiceImpl(userAttributeRepositoryMock, prdEnumRepositoryMock, prdEnumServiceMock);

    private final PrdEnumId prdEnumIdMock = mock(PrdEnumId.class);


    private final ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
    private List<String> userRoles = new ArrayList<>();
    private List<PrdEnum> prdEnums = new ArrayList<>();

    private final UserAttribute userAttributeMock = mock(UserAttribute.class);

    private PrdEnum anEnum;

    private UserAttribute userAttribute = new UserAttribute(professionalUserMock, anEnum);
    private List<UserAttribute> userAttributes =  new ArrayList<>();

    @Before
    public void setUp() {
        anEnum = new PrdEnum(prdEnumIdMock, "pui-user-manager", "SIDAM_ROLE");

        userAttributes.add(userAttribute);
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);

        prdEnums.add(anEnum);

        userRoles.add("pui-user-manager");

        when(prdEnumIdMock.getEnumType()).thenReturn("JURISD_ID");
    }

    @Test
    public void adds_user_attributes_to_user_correctly() {

        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);

        userAttributeServiceMock.addUserAttributesToUser(professionalUserMock, userRoles, prdEnums);

        assertThat(professionalUserMock.getUserAttributes()).isNotNull();

        verify(
                userAttributeRepositoryMock,
                times(1)).saveAll(any());

    }

    @Test
    public void testAddAllAttributes() {

        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"), "PROBATE", "PROBATE"));

        when(prdEnumRepositoryMock.findAll()).thenReturn(prdEnums);
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);
        when(userAttributeRepositoryMock.saveAll(any())).thenReturn(userAttributes);

        int expectSize = userAttributes.size() + 1;

        ProfessionalUser professionalUserMock = Mockito.mock(ProfessionalUser.class);
        List<String> jurisdictionIds = new ArrayList<>();
        jurisdictionIds.add("PROBATE");

        List<UserAttribute> result = userAttributeServiceMock.addUserAttributesToSuperUserWithJurisdictions(professionalUserMock, userAttributes, jurisdictionIds);

        assertThat(result.size()).isEqualTo(expectSize);

        verify(
                userAttributeRepositoryMock,
                times(1)).saveAll(any());
    }
}