package uk.gov.hmcts.reform.professionalapi.controller.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExceptionMapper;
import uk.gov.hmcts.reform.professionalapi.service.impl.ActiveOrganisationSearch;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActiveOrganisationSearchControllerTest {

    private static final String SEARCH_PATH = "/refdata/internal/v1/organisations/search";
    private static final String DEADLINE_HEADER = "X-Organisation-Search-Deadline";

    @Test
    void returnsAnExactFilteredCountAndEmptyPage() throws Exception {
        var service = mock(ActiveOrganisationSearch.class);
        when(service.search(eq("smith SW1A 1AA"), eq(14), eq(10), eq("Bearer caller"), any()))
                .thenReturn(new ActiveOrganisationSearch.Response(123, """
                        {"organisations":[],"moreAvailable":false}
                        """.getBytes(UTF_8)));
        var controller = new ActiveOrganisationSearchController(service, new ObjectMapper(), 10);
        MockMvcBuilders.standaloneSetup(controller).build()
                .perform(post(SEARCH_PATH)
                        .header("Authorization", "Bearer caller")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"searchTerm":"smith SW1A 1AA","statuses":["ACTIVE"],"page":14,"size":10}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("total_records", "123"))
                .andExpect(jsonPath("$.organisations").isEmpty())
                .andExpect(jsonPath("$.moreAvailable").value(false));
    }

    @Test
    void acceptsOmittedTermsAndPageAndSizeDefaults() throws Exception {
        var service = mock(ActiveOrganisationSearch.class);
        when(service.search(eq(""), eq(1), eq(10), eq("Bearer caller"), any()))
                .thenReturn(new ActiveOrganisationSearch.Response(0, """
                        {"organisations":[],"moreAvailable":false}
                        """.getBytes(UTF_8)));
        MockMvcBuilders.standaloneSetup(new ActiveOrganisationSearchController(service, new ObjectMapper(), 10))
                .setControllerAdvice(new ExceptionMapper()).build()
                .perform(post(SEARCH_PATH).header("Authorization", "Bearer caller")
                        .contentType(APPLICATION_JSON).content("{\"statuses\":[\"ACTIVE\"]}"))
                .andExpect(status().isOk())
                .andExpect(header().string("total_records", "0"));
    }

    @Test
    void rejectsOversizedBodiesBeforeReadingOrSearching() throws Exception {
        var service = mock(ActiveOrganisationSearch.class);
        MockMvcBuilders.standaloneSetup(new ActiveOrganisationSearchController(service, new ObjectMapper(), 10))
                .setControllerAdvice(new ExceptionMapper()).build()
                .perform(post(SEARCH_PATH).header("Authorization", "Bearer caller")
                        .contentType(APPLICATION_JSON).content(new byte[6 * 1024 * 1024 + 1]))
                .andExpect(status().isPayloadTooLarge());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @CsvSource({"garbage,400", "1,504", "100000000000000000000,400"})
    void rejectsInvalidOrExpiredUpstreamDeadlines(String deadline, int expectedStatus) throws Exception {
        var service = mock(ActiveOrganisationSearch.class);
        MockMvcBuilders.standaloneSetup(new ActiveOrganisationSearchController(service, new ObjectMapper(), 10))
                .setControllerAdvice(new ExceptionMapper()).build()
                .perform(post(SEARCH_PATH)
                        .header("Authorization", "Bearer caller")
                        .header(DEADLINE_HEADER, deadline)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"statuses":["ACTIVE"]}
                                """))
                .andExpect(status().is(expectedStatus));
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"statuses\":[\"PENDING\"]}",
        "{\"statuses\":[\"ACTIVE\"],\"page\":2.5}",
        "{\"statuses\":[\"ACTIVE\"],\"size\":0}",
        "{\"statuses\":[\"ACTIVE\"],\"page\":null}",
        "{\"statuses\":[\"ACTIVE\"],\"size\":null}",
        "{\"statuses\":[\"ACTIVE\"],\"size\":\"10\"}",
        "{\"statuses\":[\"ACTIVE\"],\"searchTerm\":123}"
    })
    void rejectsInvalidSearchRequests(String request) throws Exception {
        var service = mock(ActiveOrganisationSearch.class);
        MockMvcBuilders.standaloneSetup(new ActiveOrganisationSearchController(service, new ObjectMapper(), 10))
                .setControllerAdvice(new ExceptionMapper()).build()
                .perform(post(SEARCH_PATH).header("Authorization", "Bearer caller")
                        .contentType(APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
