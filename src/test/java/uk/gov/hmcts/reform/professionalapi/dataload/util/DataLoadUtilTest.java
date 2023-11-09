package uk.gov.hmcts.reform.professionalapi.dataload.util;


import org.apache.camel.CamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.professionalapi.dataload.helper.JrdTestSupport;

import java.sql.Timestamp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Configuration()
@ContextConfiguration(classes = DataLoadUtil.class)
public class DataLoadUtilTest extends CamelTestSupport {

    @Autowired
    DataLoadUtil dataLoadUtil;

    @Test
    public void setGlobalConstant() throws Exception {
        CamelContext camelContext = createCamelContext();
        camelContext.start();
        dataLoadUtil.setGlobalConstant(camelContext, "judicial_leaf_scheduler");
        assertNotNull("judicial_leaf_scheduler", camelContext.getGlobalOption(MappingConstants.SCHEDULER_NAME));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void removeGlobalConstant() throws Exception {
        CamelContext camelContext = mock(CamelContext.class);
        dataLoadUtil.removeGlobalConstant(camelContext);
        assertNull(camelContext.getGlobalOption(MappingConstants.SCHEDULER_NAME));
        verify(camelContext,times(1)).stop();
    }

    @Test
    public void test_getDateTimeStamp() {
        Timestamp ts = DataLoadUtil.getDateTimeStamp(JrdTestSupport.createCurrentLocalDate());
        assertNotNull(ts);
    }

    @Test
    public void test_getCurrentTimeStamp() {
        Timestamp ts = DataLoadUtil.getCurrentTimeStamp();
        assertNotNull(ts);
    }

    @Test
    public void isFileExecuted_test() throws Exception {

        CamelContext camelContext = createCamelContext();
        assertFalse(dataLoadUtil.isFileExecuted(camelContext,"filename"));
    }

    @Test
    public void isStringArraysEqual_test() throws Exception {

        String[] exp = {"one","two","three"};
        String[] act = {"one","two","three"};

        assertTrue(dataLoadUtil.isStringArraysEqual(exp,act));

    }

    @Test
    public void isStringArraysNotEqual_test() throws Exception {

        String[] exp = {"oen","too"};
        String[] act = {"one","two","three"};

        assertFalse(dataLoadUtil.isStringArraysEqual(exp,act));

    }

    @Test
    public void isStringExpEmpty_test() throws Exception {

        String[] exp = null;
        String[] act = null;

        assertFalse(dataLoadUtil.isStringArraysEqual(exp,act));

    }

    @Test
    public void isStringUnEqualLength_test() throws Exception {

        String[] exp = {"one","two"};
        String[] act = {"one","two","three"};


        assertFalse(dataLoadUtil.isStringArraysEqual(exp,act));

    }
}
