package uk.gov.hmcts.reform.professionalapi.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.*;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "professional_user")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProfessionalUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress;

    @Column(name = "STATUS")
    private String status;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID", nullable = false)
    private Organisation organisation;

    @ManyToOne
    @JoinColumn(name = "PAYMENT_ACCOUNT_ID")
    private  PaymentAccount paymentAccount;

    @LastModifiedDate
    @Column(name = "LAST_UPDATED")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "CREATED")
    private LocalDateTime created;

    public ProfessionalUser(
            String firstName,
            String lastName,
            String emailAddress,
            String status,
            Organisation organisation) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.status = status;
        this.organisation = organisation;
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getStatus() {
        return status;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public PaymentAccount getPaymentAccount() {
        return paymentAccount;
    }
}
