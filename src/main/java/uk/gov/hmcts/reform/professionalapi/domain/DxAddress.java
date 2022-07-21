package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static javax.persistence.GenerationType.AUTO;

@Entity(name = "dx_address")
@NoArgsConstructor
@Getter
@Setter
public class DxAddress implements Serializable {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "DX_NUMBER")
    private String dxNumber;

    @Column(name = "DX_EXCHANGE")
    private String dxExchange;

    @ManyToOne
    @JoinColumn(name = "CONTACT_INFORMATION_ID")
    private ContactInformation contactInformation;

    @UpdateTimestamp
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "CREATED")
    private LocalDateTime created;

    public DxAddress(String dxNumber, String dxExchange, ContactInformation contactInformation) {
        this.dxNumber = dxNumber;
        this.dxExchange = dxExchange;
        this.contactInformation = contactInformation;
    }
}
