package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class WelcomeControllerTest {

    private final WelcomeController welcomeController = new WelcomeController();

    @Test
    public void test_should_return_welcome_response() {

        ResponseEntity<String> responseEntity = welcomeController.welcome();
        String expectedMessage = "Welcome to the System Reference Data API";

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertThat(responseEntity.getBody()).contains(expectedMessage);
    }
}
