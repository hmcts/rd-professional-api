package uk.gov.hmcts.reform.professionalapi.dataload.processor;

import com.google.common.collect.ImmutableList;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.professionalapi.dataload.binder.BulkCustomerDetails;
import uk.gov.hmcts.reform.professionalapi.dataload.route.beans.RouteProperties;
import uk.gov.hmcts.reform.professionalapi.dataload.validator.JsrValidatorInitializer;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.reform.professionalapi.dataload.util.MappingConstants.ROUTE_DETAILS;

@ExtendWith(MockitoExtension.class)
public class BulkCustomerDetailsProcessorTest {

    @Spy
    BulkCustomerDetailsProcessor processor = new BulkCustomerDetailsProcessor();

    CamelContext camelContext = new DefaultCamelContext();

    Exchange exchange = new DefaultExchange(camelContext);

    @SpyBean
    @Qualifier("JsrValidatorInitializerDataload")
    JsrValidatorInitializer<BulkCustomerDetails> lovServiceJsrValidatorInitializer
        = new JsrValidatorInitializer<>();

    @Mock
    JdbcTemplate jdbcTemplate;


    @Spy
    OrganisationRepository organisationRepository;

    @Mock
    PlatformTransactionManager platformTransactionManager;

    @Mock
    ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Mock
    ConfigurableApplicationContext applicationContext;

    @BeforeEach
    void init() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        setField(lovServiceJsrValidatorInitializer, "validator", validator);
        setField(lovServiceJsrValidatorInitializer, "camelContext", camelContext);
        // setField(processor, "jdbcTemplate", jdbcTemplate);
        setField(lovServiceJsrValidatorInitializer, "jdbcTemplate", jdbcTemplate);
        setField(lovServiceJsrValidatorInitializer, "platformTransactionManager",
            platformTransactionManager
        );

        setField(processor, "bulkCustomerDetailsJsrValidatorInitializer",
            lovServiceJsrValidatorInitializer
        );
        setField(processor, "organisationRepository",
            organisationRepository
        );
        setField(processor, "logComponentName",
            "testlogger"
        );
        //setField(processor, "flagCodeQuery", "test");
        setField(processor, "applicationContext", applicationContext);
        RouteProperties routeProperties = new RouteProperties();
        routeProperties.setFileName("test");
        exchange.getIn().setHeader(ROUTE_DETAILS, routeProperties);
    }

    @Test
    @DisplayName("Test for LOV Duplicate records Case1")
    void testBulkCustomerDetails() {

        var bulkCustomerDetails = new ArrayList<BulkCustomerDetails>();
        bulkCustomerDetails.addAll(getBulkCustomerDetails());

        exchange.getIn().setBody(bulkCustomerDetails);

        Organisation organisationOne = new Organisation();
        organisationOne.setId(UUID.fromString("046b6c7f-0b8a-43b9-b35d-6489e6daee44"));
        organisationOne.setCompanyNumber("325");
        when(organisationRepository.findById(any())).thenReturn(Optional.of(organisationOne));
        processor.process(exchange);
        verify(processor, times(1)).process(exchange);



        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(2, actualLovServiceList.size());

    }

    @Test
    @DisplayName("Test for LOV Duplicate records Case2")
    void testInvalidBulkCustomerDetails() {
        var bulkCustomersList = new ArrayList<BulkCustomerDetails>();
        bulkCustomersList.addAll(getBulkCustomerDetailsTwo());

        exchange.getIn().setBody(bulkCustomersList);
        var bulkCustomerDetails = new ArrayList<BulkCustomerDetails>();
        bulkCustomerDetails.addAll(getBulkCustomerDetails());

        exchange.getIn().setBody(bulkCustomerDetails);

        Organisation organisationOne = new Organisation();
        organisationOne.setId(UUID.fromString("046b6c7f-0b8a-43b9-b35d-6489e6daee44"));
        organisationOne.setCompanyNumber("325");
        when(organisationRepository.findById(any())).thenReturn(Optional.empty());
        when(((ConfigurableApplicationContext)
            applicationContext).getBeanFactory()).thenReturn(configurableListableBeanFactory);

        processor.process(exchange);
        verify(processor, times(1)).process(exchange);

        List actualLovServiceList = (List) exchange.getMessage().getBody();
        Assertions.assertEquals(0, actualLovServiceList.size());
    }

    private List<BulkCustomerDetails> getBulkCustomerDetails() {
        return ImmutableList.of(
            BulkCustomerDetails.builder()
                .bulkCustomerId("bulko1")
                .organisationId("046b6c7f-0b8a-43b9-b35d-6489e6daee44")
                .pbaNumber("pbaNumOn1")
                .build(),
            BulkCustomerDetails.builder()
                .bulkCustomerId("bulko2")
                .organisationId("046b6c7f-0b8a-43b9-b35d-6489e6daee44")
                .pbaNumber("pbaNumOn2")
                .build()
        );
    }

    private List<BulkCustomerDetails> getBulkCustomerDetailsTwo() {
        return ImmutableList.of(
                BulkCustomerDetails.builder()
                    .bulkCustomerId("bulko1")
                    .organisationId("046b6c7f-0b8a-43b9-b35d-6489e6daee44")
                    .pbaNumber("pbaNumOn1")
                    .build(),
                BulkCustomerDetails.builder()
                    .bulkCustomerId("bulko2")
                    .organisationId("046b6c7f-0b8a-43b9-b35d-6489e6daee45")
                    .pbaNumber("pbaNumOn2")
                    .build()
            );
    }
}
