package uk.gov.hmcts.reform.professionalapi.dataload.route.beans;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileStatus {

    String fileName;

    String executionStatus;

    String auditStatus;
}
