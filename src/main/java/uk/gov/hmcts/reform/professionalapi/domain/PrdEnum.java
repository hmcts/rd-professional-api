package uk.gov.hmcts.reform.professionalapi.domain;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;
import javax.validation.constraints.Size;

@Entity
public class PrdEnum {

    @EmbeddedId
    private PrdEnumId prdEnumId;

    @Column
    @Size(max = 50)
    private String enumName;

    @Column
    @Size(max = 1024)
    private String enumDescription;

    @OneToMany(mappedBy = "prdEnum")
    private List<UserAttribute> userAttributes;

    public PrdEnum (PrdEnumId prdEnumId, String enumName, String enumDescription, List<UserAttribute> userAttributes){
        this.prdEnumId = prdEnumId;
        this.enumName = enumName;
        this.enumDescription = enumDescription;
        this.userAttributes = userAttributes;
    }

    public String getEnumName() {
        return enumName;
    }

    public PrdEnumId getPrdEnumId(){
        return prdEnumId;
    }

}



//    @OneToMany
//    @JoinColumn(name = "PRD_ENUM_CODE", nullable = false)
//    private List<String> enumCodes;