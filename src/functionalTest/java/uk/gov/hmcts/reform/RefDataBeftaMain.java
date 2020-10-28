package uk.gov.hmcts.reform;

import uk.gov.hmcts.befta.BeftaMain;

public class RefDataBeftaMain {

    private RefDataBeftaMain() {
    }

    public static void main(String[] args) {
        BeftaMain.main(args, new RefDataTestAutomationAdapter());
    }
}
