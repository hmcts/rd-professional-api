package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

import static uk.gov.hmcts.reform.professionalapi.domain.MFAStatus.EMAIL;


@Entity(name = "organisation_mfa_status")
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class OrganisationMfaStatus implements Serializable {

    @Id
    @Column(name = "ORGANISATION_ID")
    private UUID organisationId;

    @OneToOne
    @JoinColumn(name = "ORGANISATION_ID")
    @MapsId
    private Organisation organisation;

    @Column(name = "MFA_STATUS")
    @Enumerated(EnumType.STRING)
    private MFAStatus mfaStatus = EMAIL;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

}
