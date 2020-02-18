package uk.gov.hmcts.reform.professionalapi.domain;

import static javax.persistence.GenerationType.AUTO;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "user_attribute")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
public class UserAttribute implements Serializable {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Fetch(FetchMode.JOIN)
    @ManyToOne
    @JoinColumn(name = "PROFESSIONAL_USER_ID", nullable = false)
    private ProfessionalUser professionalUser;

    @Fetch(FetchMode.JOIN)
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "prd_enum_code", referencedColumnName = "enum_code"),
            @JoinColumn(name = "prd_enum_type", referencedColumnName = "enum_type")})
    private PrdEnum prdEnum;


    public UserAttribute(ProfessionalUser professionalUser, PrdEnum prdEnum) {
        this.professionalUser = professionalUser;
        this.prdEnum = prdEnum;
    }
}