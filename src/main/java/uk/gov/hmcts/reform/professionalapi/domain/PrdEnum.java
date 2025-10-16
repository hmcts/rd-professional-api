package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@Entity(name = "prd_enum")
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