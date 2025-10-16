package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.GenerationType.AUTO;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;

@Entity(name = "payment_account")
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class PaymentAccount implements Serializable {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "PBA_NUMBER")
    @Size(min = 10, max = 10)
    private String pbaNumber;

    @Column(name = "PBA_STATUS")
    @Enumerated(EnumType.STRING)
    private PbaStatus pbaStatus = PENDING;

    @Column(name = "STATUS_MESSAGE")
    private String statusMessage;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID")
    private Organisation organisation;

    @Column(name = "ORGANISATION_ID", insertable = false, updatable = false)
    private UUID organisationId;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    public PaymentAccount(String pbaNumber) {
        this.pbaNumber = pbaNumber;
    }

}
