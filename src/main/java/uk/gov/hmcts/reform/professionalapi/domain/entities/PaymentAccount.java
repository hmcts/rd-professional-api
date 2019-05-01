package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static javax.persistence.GenerationType.AUTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "payment_account")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
public class PaymentAccount {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "PBA_NUMBER")
    private String pbaNumber;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID")
    private Organisation organisation;

    @OneToMany(mappedBy = "paymentAccount")
    private List<ProfessionalUser> users;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    public PaymentAccount(String pbaNumber) {
        this.pbaNumber = pbaNumber;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    public void addUser(ProfessionalUser user) {
        if (users == null) {
            users = new ArrayList<>();
        }
        this.users.add(user);
    }
}
