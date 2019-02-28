package uk.gov.hmcts.reform.sysrefdataapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.sysrefdataapi.domain.entities.Country;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("Integration")
public class CountrySysRefDataTest {

    @Autowired
    ObjectMapper objectMapper;

    private MockMvc mockMvc;

    //@MockBean
    //private JdbcTemplate jdbcTemplate;

    private static final String APP_BASE_PATH = "/sysrefdata";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void should_get_status_200_when_get_country_by_id() throws Exception {

        //String id = String.valueOf(new Random().nextInt());
        String id = "1";
        //String countryName = "Jamaica";
        String countryName = "South Africa";
        String path = "/countries/" + id;
        Country country = new Country(id, countryName);

        //when(jdbcTemplate.queryForObject(anyString(), any(), eq(String.class))).thenReturn(countryName);

        MvcResult response = sendGetRequest(APP_BASE_PATH + path, MediaType.APPLICATION_JSON, HttpStatus.OK.value());

        Country result = objectMapper.readValue(response.getResponse().getContentAsString(), Country.class);

        assertThat(result).isEqualToComparingFieldByField(country);

    }

    @Test
    public void should_get_status_404_when_country_id_is_not_in_db() throws Exception {

        String id = String.valueOf(new Random().nextInt());
        String path = "/countries/" + id;
        //String path = "/countries/1";

        //when(jdbcTemplate.queryForObject(anyString(), any(), eq(String.class))).thenThrow(EmptyResultDataAccessException.class);

        sendGetRequest(APP_BASE_PATH + path, MediaType.APPLICATION_JSON, HttpStatus.NOT_FOUND.value());


    }

    private MvcResult sendGetRequest(final String path,
                                     final MediaType mediaType,
                                     final int expectedHttpStatus) throws Exception {

        return mockMvc.perform(get(path)
            .contentType(mediaType))
            .andExpect(status().is(expectedHttpStatus)).andReturn();


    }


}
