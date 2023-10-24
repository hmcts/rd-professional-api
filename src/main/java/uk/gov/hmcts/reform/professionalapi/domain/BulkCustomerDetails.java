package uk.gov.hmcts.reform.professionalapi.domain;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import static javax.persistence.GenerationType.AUTO;

@Entity(name = "bulk_customer_details")
@NoArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BulkCustomerDetails implements Serializable {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "bulk_customer_id")
    @Size(max = 64)
    private String bulkCustomerId;

    @Column(name = "sidam_id")
    @Size(max = 64)
    private String sidamId;

    @Column(name = "pba_number")
    @Size(max = 255)
    private String pbaNumber;

    @Column(name = "organisation_id")
    @Size(max = 7)
    private String organisationId;

    @ManyToOne
    @JoinColumn(name = "organisation_id", referencedColumnName = "ORGANISATION_IDENTIFIER",
        insertable = false, updatable = false, nullable = false)
    private Organisation organisation;
}
