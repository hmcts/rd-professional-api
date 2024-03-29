package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;

import static javax.persistence.GenerationType.AUTO;

@Entity(name = "contact_information")
@NoArgsConstructor
@Getter
@Setter
public class ContactInformation implements Serializable {

    @Column(name = "uprn")
    @Size(max = 14)
    private String uprn;

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "ADDRESS_LINE1")
    @Size(max = 150)
    private String addressLine1;

    @Column(name = "ADDRESS_LINE2")
    @Size(max = 150)
    private String addressLine2;

    @Column(name = "ADDRESS_LINE3")
    @Size(max = 50)
    private String addressLine3;

    @Column(name = "TOWN_CITY")
    @Size(max = 50)
    private String townCity;

    @Column(name = "COUNTY")
    @Size(max = 50)
    private String county;

    @Column(name = "COUNTRY")
    @Size(max = 50)
    private String country;

    @Column(name = "POSTCODE")
    @Size(max = 14)
    private String postCode;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID", nullable = false)
    private Organisation organisation;

    @UpdateTimestamp
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "CREATED")
    private LocalDateTime created;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "contactInformation", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<DxAddress> dxAddresses = new ArrayList<>();

    public void addDxAddress(DxAddress dxAddress) {
        dxAddresses.add(dxAddress);
    }

    public List<DxAddress> getDxAddresses() {
        return dxAddresses;
    }



}
