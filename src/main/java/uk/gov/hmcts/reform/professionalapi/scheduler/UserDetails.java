package uk.gov.hmcts.reform.professionalapi.scheduler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.data.ingestion.camel.domain.CommonCsvField;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

@Component
@Setter
@Getter
@CsvRecord(separator = ",", crlf = "UNIX", skipFirstLine = true, skipField = true)
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserDetails extends CommonCsvField implements Serializable {

    @DataField(pos = 1, columnName = "id")
    @NotEmpty(message = "ID is missing")
    @SuppressWarnings("all")
    private String id;

    @DataField(pos = 2, columnName = "first_name")
    @NotEmpty(message = "First Name is missing")
    private String firstName;

    @DataField(pos = 3, columnName = "last_name")
    @NotEmpty(message = "Last Name is missing")
    private String lastName;
}
