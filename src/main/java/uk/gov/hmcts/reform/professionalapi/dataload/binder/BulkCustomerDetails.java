package uk.gov.hmcts.reform.professionalapi.dataload.binder;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.dataload.domain.CommonCsvField;

import java.io.Serializable;


// TODO validation frameworks needs to be implemented


@Component
@Setter
@Getter
@CsvRecord(separator = ",", crlf = "UNIX", skipFirstLine = true, skipField = true)
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BulkCustomerDetails extends CommonCsvField implements Serializable {


    @DataField(pos = 1, columnName = "Organisation_ID")
    @NotEmpty(message = "organisation id is missing")
    private String organisationId;

    @DataField(pos = 2, columnName = "Bulk_Customer_ID")
    @NotEmpty(message = "bulkCustomer id is missing")
    private String bulkCustomerId;

    @DataField(pos = 3, columnName = "Sidam_ID")
    private String sidamId;

    @DataField(pos = 4, columnName = "PBA")
    @NotEmpty(message = "pba number is missing")
    private String pbaNumber;



}
