package uk.gov.hmcts.reform.professionalapi.domain.entities;

import static javax.persistence.GenerationType.AUTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity(name = "organisation")
@NoArgsConstructor
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

    @UpdateTimestamp
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(name = "CREATED")
    private LocalDateTime created;

    public Organisation(
            String name,
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

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<ProfessionalUser> getUsers() {
        return users;
    }

    public List<PaymentAccount> getPaymentAccounts() {
        return paymentAccounts;
    }

    public String getStatus() {
        return status;
    }
}
