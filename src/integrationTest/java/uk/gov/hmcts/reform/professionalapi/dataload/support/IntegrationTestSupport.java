package uk.gov.hmcts.reform.professionalapi.dataload.support;

import org.springframework.util.ResourceUtils;

public interface IntegrationTestSupport {

    static void setSourcePath(String path, String propertyPlaceHolder) throws Exception {

        String loadFile = ResourceUtils.getFile(path).getCanonicalPath();

        if (loadFile.endsWith(".csv")) {
            int lastSlash = loadFile.lastIndexOf("/");
            String result = loadFile.substring(0, lastSlash);
            String fileName = loadFile.substring(lastSlash + 1);

            System.setProperty(propertyPlaceHolder, "file:"
                + result + "?fileName=" + fileName + "&noop=true");
        } else {
            System.setProperty(propertyPlaceHolder, "file:" + loadFile.replaceFirst("/", ""));
        }
    }
}
