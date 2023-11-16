package uk.gov.hmcts.reform.professionalapi.dataload.support;

import static  uk.gov.hmcts.reform.professionalapi.dataload.support.IntegrationTestSupport.setSourcePath;

public interface ParentIntegrationTestSupport extends IntegrationTestSupport {

    String[] fileWithInvalidJsr = {"classpath:sourceFiles/bulk_customer_ids.csv"};

    static void setSourceData(String... files) throws Exception {
        System.setProperty("parent.file.name", files[0]);
        setSourcePath(files[0],
            "parent.file.path");
    }
}

