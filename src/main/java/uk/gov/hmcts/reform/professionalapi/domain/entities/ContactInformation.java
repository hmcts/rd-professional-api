package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static javax.persistence.GenerationType.AUTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;



@Entity(name = "contact_information")
@NoArgsConstructor
@Getter
@Setter
public class ContactInformation {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "ADDRESS_LINE1")
    @Size(max = 150)
    private String addressLine1;

    @Column(name = "ADDRESS_LINE2")
    @Size(max = 50)
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

    @OneToMany(mappedBy = "contactInformation")
    private List<DxAddress> dxAddresses = new ArrayList<>();

    public ContactInformation(String addressLine1, String addressLine2, String addressLine3, String townCity,
                              String county, String country, String postCode, Organisation organisation) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.addressLine3 = addressLine3;
        this.townCity = townCity;
        this.county = county;
        this.country = country;
        this.postCode = postCode;
        this.organisation = organisation;
    }

    public void addDxAddress(DxAddress dxAddress) {
        dxAddresses.add(dxAddress);
    }

    public List<DxAddress> getDxAddresses() {
        return dxAddresses;
    }



}
