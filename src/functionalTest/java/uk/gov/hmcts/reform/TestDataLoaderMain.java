package uk.gov.hmcts.reform;

public class TestDataLoaderMain {

    private TestDataLoaderMain() {
    }

    public static void main(String[] args) {
        new RefDataTestAutomationAdapter().doLoadTestData();
    }

}
