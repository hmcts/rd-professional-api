package uk.gov.hmcts.reform.professionalapi.domain;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class PrdEnum implements Serializable {

    @EmbeddedId
    private PrdEnumId prdEnumId;

    @Column
    @Size(max = 50)
    private String enumName;

    @Column
    @Size(max = 1024)
    private String enumDesc;

    @OneToMany(mappedBy = "prdEnum")
    private List<UserAttribute> userAttributes;

    @Column
    private String enabled;

    public PrdEnum(PrdEnumId prdEnumId, String enumName, String enumDescription) {
        this.prdEnumId = prdEnumId;
        this.enumName = enumName;
        this.enumDesc = enumDescription;
    }

    public String getEnumName() {
        return enumName;
    }

    public PrdEnumId getPrdEnumId() {
        return prdEnumId;
    }

    public String getEnumDescription() {
        return enumDesc;
    }

}