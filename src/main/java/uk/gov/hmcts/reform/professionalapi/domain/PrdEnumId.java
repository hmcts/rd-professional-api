package uk.gov.hmcts.reform.professionalapi.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
