package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.hmcts.reform.professionalapi.domain.entities.Country;
import uk.gov.hmcts.reform.professionalapi.domain.entities.SystemRefData;
import uk.gov.hmcts.reform.professionalapi.domain.service.ResourceRetriever;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.SysRefDataController;

@RunWith(MockitoJUnitRunner.class)
public class SysRefDataControllerTest {

    @Mock private ResourceRetriever resourceRetriever;

    @InjectMocks
    private SysRefDataController sysRefDataController;

    @Test
    public void shouldGetCountryAsExpected() {

        String id = String.valueOf(new Random().nextInt());
        Country expected = new Country(id, "South Africa");

        when(resourceRetriever.getResource(id)).thenReturn(expected);

        ResponseEntity<SystemRefData> response = sysRefDataController.getCountry(id);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Country.class);

        Country country = (Country) response.getBody();

        assertThat(country).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void shouldPropagateThrownException() {

        EmptyResultDataAccessException mockEx = mock(EmptyResultDataAccessException.class);

        String id = String.valueOf(new Random().nextInt());
        when(resourceRetriever.getResource(id)).thenThrow(mockEx);

        assertThatThrownBy(() -> sysRefDataController.getCountry(id))
            .isInstanceOf(EmptyResultDataAccessException.class);

    }

}
