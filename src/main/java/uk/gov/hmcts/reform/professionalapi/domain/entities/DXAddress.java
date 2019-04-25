package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static javax.persistence.GenerationType.AUTO;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.NoArgsConstructor;

@Entity(name = "dx_address")
@NoArgsConstructor
public class DXAddress {

	@Id
	@GeneratedValue(strategy = AUTO)
	private UUID id;

	@Column(name = "DX_NUMBER", length = 13)
	private String dxNumber;

	@Column(name = "DX_EXCHANGE", length = 20)
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

	public DXAddress(String dxNumber, String dxExchange, ContactInformation contactInformation) {
		this.dxNumber = dxNumber;
		this.dxExchange = dxExchange;
		this.contactInformation = contactInformation;
	}

	public UUID getId() {
		return id;
	}

	public String getDxNumber() {
		return dxNumber;
	}

	public String getDxExchange() {
		return dxExchange;
	}
	
	public ContactInformation getContactInformation() {
		return contactInformation;
	}
}
