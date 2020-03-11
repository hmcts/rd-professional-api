package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;

import feign.FeignException;
import feign.Request;
import feign.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.JurisdictionFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.JurisdictionUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;


public class JurisdictionServiceImplTest {

    @MockBean
    private JurisdictionFeignClient jurisdictionFeignClient = mock(JurisdictionFeignClient.class);
    private JurisdictionServiceImpl jurisdictionServiceImpl = new JurisdictionServiceImpl();
    private JurisdictionUserCreationRequest request;
    private AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);
    private Response response = Response.builder().status(200).reason("OK").body(mock(Response.Body.class)).request(mock(Request.class)).build();

    private List<UserAttribute> attributeList = new ArrayList<>();
    private List<Jurisdiction> jurisdictions = new ArrayList<>();
    private Organisation organisation = new Organisation("some-org-name", null, "PENDING", null, null, null);
    private ProfessionalUser user = new ProfessionalUser("some-fname", "some-lname", "some@hmcts.net", organisation);
    private final PrdEnumId prdEnumId1 = new PrdEnumId(10, "JURISD_ID");
    private final PrdEnumId prdEnumId2 = new PrdEnumId(13, "JURISD_ID");
    private final PrdEnumId prdEnumId3 = new PrdEnumId(3, "PRD_ROLE");
    private final PrdEnum anEnum1 = new PrdEnum(prdEnumId1, "PROBATE", "JURISD_ID");
    private final PrdEnum anEnum2 = new PrdEnum(prdEnumId2, "BULKSCAN", "JURISD_ID");
    private final PrdEnum anEnum3 = new PrdEnum(prdEnumId3, "pui-case-manager", "PRD_ROLE");
    private final UserAttribute userAttribute1 = new UserAttribute(user, anEnum1);
    private final UserAttribute userAttribute2 = new UserAttribute(user, anEnum2);
    private final UserAttribute userAttribute3 = new UserAttribute(user, anEnum3);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        attributeList.add(userAttribute1);
        attributeList.add(userAttribute2);
        attributeList.add(userAttribute3);
        user.setUserAttributes(attributeList);

        ReflectionTestUtils.setField(jurisdictionServiceImpl, "jurisdictionFeignClient", jurisdictionFeignClient);
        ReflectionTestUtils.setField(jurisdictionServiceImpl, "authTokenGenerator", authTokenGenerator);

        Jurisdiction jurisdiction1 = new Jurisdiction();
        jurisdiction1.setId("Probate");
        Jurisdiction jurisdiction2 = new Jurisdiction();
        jurisdiction2.setId("Bulk Scanning");
        jurisdictions.add(jurisdiction1);
        jurisdictions.add(jurisdiction2);

        request = new JurisdictionUserCreationRequest(UUID.randomUUID().toString(), jurisdictions);
    }

    @Test
    public void should_create_jurisdictionUserProfileRequest_for_Super_User() {
        JurisdictionUserCreationRequest request = jurisdictionServiceImpl.createJurisdictionUserProfileRequestForSuperUser(user);
        assertThat(request).isNotNull();
        assertThat(request.getId()).isEqualTo("some@hmcts.net");
        assertThat(request.getJurisdictions()).isNotEmpty();
        assertThat(request.getJurisdictions().size()).isEqualTo(2);
        assertThat(request.getJurisdictions().get(0).getId()).isEqualTo("PROBATE");
        assertThat(request.getJurisdictions().get(1).getId()).isEqualTo("BULKSCAN");

    }

    @Test(expected = Test.None.class)
    public void should_call_ccd_without_exception() {
        when(jurisdictionFeignClient.createJurisdictionUserProfile(any(), any(), any(JurisdictionUserCreationRequest.class))).thenReturn(response);
        when(authTokenGenerator.generate()).thenReturn("s2sToken");

        jurisdictionServiceImpl.callCcd(request, "some@hmcts.net");

        verify(jurisdictionFeignClient, times(1)).createJurisdictionUserProfile(any(), any(), any(JurisdictionUserCreationRequest.class));
    }

    @Test(expected = ExternalApiException.class)
    public void should_throw_error_when_ccd_returns_error() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(400);

        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(jurisdictionFeignClient.createJurisdictionUserProfile("some@hmcts.net", "s2sToken", request)).thenThrow(feignException);

        jurisdictionServiceImpl.callCcd(request, "some@hmcts.net");

        verify(jurisdictionFeignClient, times(1)).createJurisdictionUserProfile("some@hmcts.net", "s2sToken", request);
    }

    @Test(expected = ExternalApiException.class)
    public void should_throw_error_when_ccd_returns_error_less_than_zero() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.status()).thenReturn(-1);

        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(jurisdictionFeignClient.createJurisdictionUserProfile("some@hmcts.net", "s2sToken", request)).thenThrow(feignException);

        jurisdictionServiceImpl.callCcd(request, "some@hmcts.net");

        verify(jurisdictionFeignClient, times(1)).createJurisdictionUserProfile("some@hmcts.net", "s2sToken", request);
    }

    @Test(expected = ExternalApiException.class)
    public void should_throw_error_when_ccd_returns_403() {
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        response = response.toBuilder().status(403).build();

        when(jurisdictionFeignClient.createJurisdictionUserProfile(any(), any(), any(JurisdictionUserCreationRequest.class))).thenReturn(response);
        jurisdictionServiceImpl.callCcd(request, "some@hmcts.net");

        verify(jurisdictionFeignClient, times(1)).createJurisdictionUserProfile(any(), any(), any(JurisdictionUserCreationRequest.class));
    }

    @Test(expected = ExternalApiException.class)
    public void should_throw_error_when_ccd_returns_null_response() {
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(jurisdictionFeignClient.createJurisdictionUserProfile("some@hmcts.net", "s2sToken", request)).thenReturn(null);

        jurisdictionServiceImpl.callCcd(request, "some@hmcts.net");

        verify(jurisdictionFeignClient, times(1)).createJurisdictionUserProfile("some@hmcts.net", "s2sToken", request);
    }

    @Test(expected = Test.None.class)
    public void should_propagate_jurisdiction_ids_for_super_user_To_ccd_without_exception() {
        when(jurisdictionFeignClient.createJurisdictionUserProfile(any(), any(), any(JurisdictionUserCreationRequest.class))).thenReturn(response);
        when(authTokenGenerator.generate()).thenReturn("s2sToken");

        jurisdictionServiceImpl.propagateJurisdictionIdsForSuperUserToCcd(user, "some@hmcts.net");

        verify(jurisdictionFeignClient, times(1)).createJurisdictionUserProfile(any(), any(), any(JurisdictionUserCreationRequest.class));
    }

    @Test(expected = Test.None.class)
    public void should_propagate_jurisdiction_ids_for_new_user_to_ccd_without_exception() {
        when(jurisdictionFeignClient.createJurisdictionUserProfile(any(), any(), any(JurisdictionUserCreationRequest.class))).thenReturn(response);
        when(authTokenGenerator.generate()).thenReturn("s2sToken");

        jurisdictionServiceImpl.propagateJurisdictionIdsForNewUserToCcd(createJurisdictions(), "some@hmcts.net", "some@hmcts.net");

        verify(jurisdictionFeignClient, times(1)).createJurisdictionUserProfile(any(), any(), any(JurisdictionUserCreationRequest.class));
    }
}