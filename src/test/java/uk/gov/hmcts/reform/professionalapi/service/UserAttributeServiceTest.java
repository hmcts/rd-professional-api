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
import org.junit.Test;

import org.mockito.Mockito;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.PrdEnumServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;


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

    private final UserAttribute userAttributeMock = mock(UserAttribute.class);


    @Before
    public void setUp() {
        PrdEnum anEnum = new PrdEnum(prdEnumId, "pui-user-manager", "SIDAM_ROLE");

        prdEnums.add(anEnum);

        userRoles.add("pui-user-manager");
    }

    @Test
    public void adds_user_attributes_to_user_correctly() {

        List<UserAttribute> userAttributes =  new ArrayList<>();
        PrdEnum anEnum = new PrdEnum(prdEnumId, "pui-user-manager", "SIDAM_ROLE");

        UserAttribute userAttribute = new UserAttribute(professionalUser, anEnum);
        userAttributes.add(userAttribute);
        when(prdEnumService.findAllPrdEnums()).thenReturn(prdEnums);

        userAttributeService.addUserAttributesToUser(professionalUser, userRoles, prdEnums);

        assertThat(professionalUser.getUserAttributes()).isNotNull();

        verify(
                userAttributeRepository,
                times(1)).saveAll(any());

    }

    @Test
    public void testAddAllAttributes() {
        prdEnums.add(new PrdEnum(new PrdEnumId(0, "SIDAM_ROLE"), "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(1, "SIDAM_ROLE"), "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(2, "SIDAM_ROLE"), "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(3, "SIDAM_ROLE"), "pui-user-manager", "SIDAM_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(4, "ADMIN_ROLE"), "organisation-admin", "ADMIN_ROLE"));
        prdEnums.add(new PrdEnum(new PrdEnumId(10, "JURISD_ID"), "PROBATE", "PROBATE"));

        userRoles.add("pui-user-manager");
        userRoles.add("pui-organisation-manager");
        userRoles.add("pui-finance-manager");
        userRoles.add("pui-case-manager");
        userRoles.add("organisation-admin");

        List<UserAttribute> attributes = new ArrayList<>();
        attributes.add(userAttributeMock);
        int expectSize = attributes.size() + 1;

//        when(prdEnumRepositoryMock.findAll()).thenReturn(prdEnums);
//        when(prdEnumServiceMock.findAllPrdEnums()).thenReturn(prdEnums);
//        when(userAttributeRepositoryMock.saveAll(any())).thenReturn(attributes);

        ProfessionalUser professionalUserMock = Mockito.mock(ProfessionalUser.class);
        List<String> jurisdictionIds = new ArrayList<>();

        List<UserAttribute> result = userAttributeService.addUserAttributesToSuperUserWithJurisdictions(professionalUserMock, attributes, jurisdictionIds);

        assertThat(result.size()).isEqualTo(expectSize);

//        OrganisationResponse organisationResponse =
//                organisationServiceImplMock.createOrganisationFrom(organisationCreationRequest);
//
//        verify(
//                userAttributeRepositoryMock,
//                times(1)).saveAll(any());
    }
}