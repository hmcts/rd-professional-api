package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.GenerationType.AUTO;

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
