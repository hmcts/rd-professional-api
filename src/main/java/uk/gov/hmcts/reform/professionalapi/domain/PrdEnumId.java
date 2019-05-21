package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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

    public PrdEnumId(int enumCode, String enumType) {
        this.enumCode = enumCode;
        this.enumType = enumType;
    }

    public int getEnumCode() {
        return enumCode;
    }

    public String getEnumType() {
        return enumType;
    }

}
