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
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "organisation")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Organisation {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @OneToMany(mappedBy = "organisation")
    private List<ProfessionalUser> users = new ArrayList<>();

    @OneToMany(mappedBy = "organisation")
    private List<PaymentAccount> paymentAccounts = new ArrayList<>();

    @Column(name = "STATUS")
    private String status;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    public Organisation(String name,
                        String status) {

        this.name = name;
        this.status = status;
    }

    public void addProfessionalUser(ProfessionalUser professionalUser) {
        users.add(professionalUser);
    }

    public void addPaymentAccount(PaymentAccount paymentAccount) {
        paymentAccounts.add(paymentAccount);
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
