package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountMapId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "PROFESSIONAL_USER_ID",insertable = false,
            updatable = false, nullable = false)
    private ProfessionalUser professionalUser;

    @ManyToOne
    @JoinColumn(name = "PAYMENT_ACCOUNT_ID", insertable = false,
            updatable = false, nullable = false)
    private PaymentAccount paymentAccount;

}
