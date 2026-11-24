package uk.gov.hmcts.reform.professionalapi.dataload.util;


import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.professionalapi.dataload.helper.JrdTestSupport;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@CamelSpringBootTest
@Configuration()
@ContextConfiguration(classes = DataLoadUtil.class)
class DataLoadUtilTest {

    @Autowired
    DataLoadUtil dataLoadUtil;

    @Test
    void setGlobalConstant() {
        CamelContext camelContext = new DefaultCamelContext();
        camelContext.start();
        dataLoadUtil.setGlobalConstant(camelContext, "judicial_leaf_scheduler");
        assertNotNull("judicial_leaf_scheduler", camelContext.getGlobalOption(MappingConstants.SCHEDULER_NAME));
    }

    @Test
    void removeGlobalConstant() throws Exception {
        CamelContext camelContext = mock(CamelContext.class);
        dataLoadUtil.removeGlobalConstant(camelContext);
        assertNull(camelContext.getGlobalOption(MappingConstants.SCHEDULER_NAME));
        verify(camelContext,times(1)).stop();
    }

    @Test
    void test_getDateTimeStamp() {
        Timestamp ts = DataLoadUtil.getDateTimeStamp(JrdTestSupport.createCurrentLocalDate());
        assertNotNull(ts);
    }

    @Test
    void test_getCurrentTimeStamp() {
        Timestamp ts = DataLoadUtil.getCurrentTimeStamp();
        assertNotNull(ts);
    }

    @Test
    void isFileExecuted_test() {
        CamelContext camelContext = new DefaultCamelContext();
        assertFalse(DataLoadUtil.isFileExecuted(camelContext,"filename"));
    }

    @Test
    void isStringArraysEqual_test() {
        String[] exp = {"one","two","three"};
        String[] act = {"one","two","three"};

        assertTrue(DataLoadUtil.isStringArraysEqual(exp,act));
    }

    @Test
    void isStringArraysNotEqual_test() {
        String[] exp = {"oen","too"};
        String[] act = {"one","two","three"};

        assertFalse(DataLoadUtil.isStringArraysEqual(exp,act));
    }

    @Test
    void isStringExpEmpty_test() {
        String[] exp = null;
        String[] act = null;

        assertFalse(DataLoadUtil.isStringArraysEqual(exp,act));
    }

    @Test
    void isStringUnEqualLength_test() {
        String[] exp = {"one","two"};
        String[] act = {"one","two","three"};

        assertFalse(DataLoadUtil.isStringArraysEqual(exp,act));
    }
}
