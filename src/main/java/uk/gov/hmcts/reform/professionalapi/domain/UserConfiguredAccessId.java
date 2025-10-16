package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;


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
