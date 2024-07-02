package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.UUID;

import static jakarta.persistence.GenerationType.AUTO;

@Entity(name = "user_attribute")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
public class UserAttribute implements Serializable {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "PROFESSIONAL_USER_ID", nullable = false)
    private ProfessionalUser professionalUser;

    @ManyToOne
    @JoinColumn(name = "prd_enum_code", referencedColumnName = "enum_code")
    @JoinColumn(name = "prd_enum_type", referencedColumnName = "enum_type")
    private PrdEnum prdEnum;


    public UserAttribute(ProfessionalUser professionalUser, PrdEnum prdEnum) {
        this.professionalUser = professionalUser;
        this.prdEnum = prdEnum;
    }
}