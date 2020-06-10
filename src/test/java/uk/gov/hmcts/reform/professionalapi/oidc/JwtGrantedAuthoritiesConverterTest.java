package uk.gov.hmcts.reform.professionalapi.oidc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.constants.TestConstants;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;

public class JwtGrantedAuthoritiesConverterTest {


    JwtGrantedAuthoritiesConverter converter;

    IdamRepository idamRepositoryMock;

    UserInfo userInfoMock;

    Jwt jwtMock;

    @Before
    public void setUp() {

        idamRepositoryMock = mock(IdamRepository.class);
        userInfoMock = mock(UserInfo.class);
        jwtMock = mock(Jwt.class);
        converter = new JwtGrantedAuthoritiesConverter(idamRepositoryMock);

    }

    @Test
    public void test_shouldReturnEmptyAuthorities() {

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(idamRepositoryMock,times(0)).getUserInfo(anyString());

    }

    @Test
    public void test_shouldReturnEmptyAuthoritiesWhenClaimNotAvailable() {

        when(jwtMock.containsClaim(anyString())).thenReturn(false);
        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(idamRepositoryMock,times(0)).getUserInfo(anyString());
    }

    @Test
     public void test_shouldReturnEmptyAuthoritiesWhenClaimValueNotEquals() {

        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("Test");
        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(idamRepositoryMock,times(0)).getUserInfo(anyString());
    }

    @Test
    public void test_shouldReturnEmptyAuthoritiesWhenIdamReturnsNoUsers() {

        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");
        List<String> roles = new ArrayList<String>();
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);
        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(idamRepositoryMock,times(1)).getUserInfo(anyString());

    }

    @Test
    public void test_shouldReturnAuthoritiesWhenIdamReturnsUserRoles() {

        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");

        List<String> roles = new ArrayList<String>();
        roles.add(TestConstants.PUI_CASE_MANAGER);
        roles.add(TestConstants.PUI_FINANCE_MANAGER);
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);
        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        verify(idamRepositoryMock,times(1)).getUserInfo(anyString());
    }
}
