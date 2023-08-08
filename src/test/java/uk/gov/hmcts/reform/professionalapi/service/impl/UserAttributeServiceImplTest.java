package uk.gov.hmcts.reform.professionalapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

class UserAttributeServiceImplTest {

    private final UserAttributeRepository userAttributeRepositoryMock = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepositoryMock = mock(PrdEnumRepository.class);
    private final PrdEnumServiceImpl prdEnumServiceMock = mock(PrdEnumServiceImpl.class);
    private final UserAttributeServiceImpl sut
            = new UserAttributeServiceImpl(userAttributeRepositoryMock, prdEnumRepositoryMock, prdEnumServiceMock);

    private final PrdEnumId prdEnumIdMock = new PrdEnumId(1, "SIDAM_ROLE");


    private final Organisation organisation = new Organisation("some-org-name", null, "PENDING",
            null, null, null);
    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname", "test@test.com", organisation);
    private final List<String> userRoles = new ArrayList<>();
    private final List<PrdEnum> prdEnums = new ArrayList<>();

    private final List<PrdEnum> prdEnums1 = new ArrayList<>();
    private PrdEnum anEnum;
    private PrdEnum anEnum1;
    private final UserAttribute userAttribute = new UserAttribute(professionalUser, anEnum);
    private final List<UserAttribute> userAttributes =  new ArrayList<>();

    @BeforeEach
    void setUp() {
        anEnum = new PrdEnum(prdEnumIdMock, "pui-user-manager", "prd-description");
        anEnum1 = new PrdEnum(prdEnumIdMock, "pui-user-manager", "prd-description-test");
        userAttributes.add(userAttribute);
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);
        prdEnums.add(anEnum);
        prdEnums1.add(anEnum1);
        userRoles.add("pui-user-manager");
    }

    @Test
    void test_adds_user_attributes_to_user_correctly() {
        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);

        sut.addUserAttributesToUser(professionalUser, userRoles, prdEnums);

        assertThat(professionalUser.getUserAttributes()).isNotNull();
        verify(userAttributeRepositoryMock, times(1)).saveAll(any());
    }

    @Test
    void test_adds_super_user_attributes_to_user_correctly() {
        List<UserAttribute> userAttributes = new ArrayList<>();
        userAttributes.add(new UserAttribute(professionalUser, prdEnums.get(0)));

        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);

        List<UserAttribute> userAttributeResponse = sut.addUserAttributesToSuperUser(professionalUser, userAttributes);

        assertThat(userAttributeResponse).isNotNull().isNotEmpty();
        assertThat(professionalUser.getUserAttributes()).isNotNull();
        assertThat(userAttributeResponse.get(0).getPrdEnum().getEnumName()).isEqualTo("pui-user-manager");
        assertThat(userAttributeResponse.get(0).getPrdEnum().getPrdEnumId().getEnumCode()).isEqualTo(1);
        assertThat(userAttributeResponse.get(0).getPrdEnum().getPrdEnumId().getEnumType()).isEqualTo("SIDAM_ROLE");
        assertThat(userAttributeResponse.get(0).getPrdEnum().getEnumDescription()).isEqualTo("prd-description");

        verify(userAttributeRepositoryMock, times(1)).saveAll(any());
    }

    @Test
    void test_adds_super_user_attributes_to_user_correctly2() {
        List<UserAttribute> userAttributes = new ArrayList<>();
        userAttributes.add(new UserAttribute(professionalUser, prdEnums1.get(0)));

        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums1);

        List<UserAttribute> userAttributeResponse = sut.addUserAttributesToSuperUser(professionalUser, userAttributes);

        assertThat(userAttributeResponse).isNotNull().isNotEmpty();
        assertThat(professionalUser.getUserAttributes()).isNotNull();
        assertThat(userAttributeResponse.get(0).getPrdEnum().getEnumName()).isEqualTo("pui-user-manager");
        assertThat(userAttributeResponse.get(0).getPrdEnum().getPrdEnumId().getEnumCode()).isEqualTo(1);
        assertThat(userAttributeResponse.get(0).getPrdEnum().getPrdEnumId().getEnumType()).isEqualTo("SIDAM_ROLE");
        assertThat(userAttributeResponse.get(0).getPrdEnum().getEnumDescription()).isEqualTo("prd-description-test");
        verify(userAttributeRepositoryMock, times(1)).saveAll(any());
    }

    @Test
    void test_isValidEnumType() {
        boolean response = sut.isValidEnumType("SIDAM_ROLE");
        assertThat(response).isTrue();

        boolean response1 = sut.isValidEnumType("ADMIN_ROLE");
        assertThat(response1).isTrue();

        boolean response2 = sut.isValidEnumType("INVALID_ROLE");
        assertThat(response2).isFalse();

        boolean response3 = sut.isValidEnumType("JURISD_ID");
        assertThat(response3).isFalse();


    }
}