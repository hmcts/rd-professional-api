package uk.gov.hmcts.reform.professionalapi.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.GenerationType.AUTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity(name = "audit")
@NoArgsConstructor
@Getter
@Setter
public class Audit implements Serializable {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "ORGANISATION_ID")
    private String organisationIdentifier;

    @Column(name = "CHANGE_DETAILS")
    @Size(max = 550)
    private String changeDetails;

    @Column(name = "UPDATED_BY")
    @Size(max = 550)
    private String updatedBy;

    @Column(name = "CHANGE_ACTION")
    @Size(max = 550)
    private String changeAction;

    @UpdateTimestamp
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "CREATED")
    private LocalDateTime created;


}
