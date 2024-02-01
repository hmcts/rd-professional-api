package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserConfiguredAccessId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "PROFESSIONAL_USER_ID",insertable = false,
            updatable = false, nullable = false)
    private ProfessionalUser professionalUser;

    @Column(name = "jurisdiction_id")
    private String jurisdictionId;

    @Column(name = "organisation_profile_id")
    private String organisationProfileId;

    @Column(name = "access_type_id")
    private String accessTypeId;

}
