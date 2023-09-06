package uk.gov.hmcts.reform.professionalapi.dataload.service.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Audit {
    private String fileName;
    private Date schedulerStartTime;
    private String status;
}
