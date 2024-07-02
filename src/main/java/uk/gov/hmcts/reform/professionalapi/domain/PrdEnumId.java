package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class PrdEnumId implements Serializable {

    @Column(name = "enum_code")
    @NotNull
    @Size(max = 1)
    private int enumCode;

    @Column(name = "enum_type")
    @NotNull
    @Size(max = 50)
    private String enumType;

}
