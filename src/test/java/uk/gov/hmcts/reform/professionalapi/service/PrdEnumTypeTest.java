package uk.gov.hmcts.reform.professionalapi.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PrdEnumTypeTest {

    @Test
    public void prdEnumTypeTest() {

        PrdEnumType prdEnumType = PrdEnumType.ADMIN_ROLE;
        assertEquals(PrdEnumType.valueOf("ADMIN_ROLE"), prdEnumType);
    }
}
