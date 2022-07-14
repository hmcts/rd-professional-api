package uk.gov.hmcts.reform.professionalapi.oidc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.constants.TestConstants;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtGrantedAuthoritiesConverterTest {

    JwtGrantedAuthoritiesConverter converter;
    IdamRepository idamRepositoryMock;
    UserInfo userInfoMock;
    Jwt jwtMock;

    @BeforeEach
    void setUp() {
        idamRepositoryMock = mock(IdamRepository.class);
        userInfoMock = mock(UserInfo.class);
        jwtMock = mock(Jwt.class);
        converter = new JwtGrantedAuthoritiesConverter(idamRepositoryMock);
    }

    @Test
    void test_shouldReturnEmptyAuthorities() {
        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(idamRepositoryMock, times(0)).getUserInfo(anyString());
    }

    @Test
    void test_shouldReturnEmptyAuthoritiesWhenClaimNotAvailable() {
        when(jwtMock.containsClaim(anyString())).thenReturn(false);

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(idamRepositoryMock, times(0)).getUserInfo(anyString());
    }

    @Test
    void test_shouldReturnEmptyAuthoritiesWhenClaimValueNotEquals() {
        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("Test");

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(idamRepositoryMock, times(0)).getUserInfo(anyString());
    }

    @Test
    void test_shouldReturnEmptyAuthoritiesWhenIdamReturnsNoUsers() {
        List<String> roles = new ArrayList<>();

        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(jwtMock, times(1)).getTokenValue();
        verify(userInfoMock, times(1)).getRoles();
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());

    }

    @Test
    void test_shouldReturnAuthoritiesWhenIdamReturnsUserRoles() {
        List<String> roles = new ArrayList<>();
        roles.add(TestConstants.PUI_CASE_MANAGER);
        roles.add(TestConstants.PUI_FINANCE_MANAGER);

        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(jwtMock, times(1)).getTokenValue();
        verify(userInfoMock, times(1)).getRoles();
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());
    }

    @Test
    void test_shouldReturnUserInfo() {
        List<String> roles = new ArrayList<>();
        roles.add(TestConstants.PUI_CASE_MANAGER);
        roles.add(TestConstants.PUI_FINANCE_MANAGER);

        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);

        converter.convert(jwtMock);
        UserInfo userInfo = converter.getUserInfo();

        assertThat(userInfo).isNotNull();
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(jwtMock, times(1)).getTokenValue();
        verify(userInfoMock, times(1)).getRoles();
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());
    }
}