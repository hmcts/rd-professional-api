package uk.gov.hmcts.reform.professionalapi.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class UserAccountMapId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "PROFESSIONAL_USER_ID",insertable = false,
            updatable = false, nullable = false)
    private ProfessionalUser professionalUser;

    @ManyToOne
    @JoinColumn(name = "PAYMENT_ACCOUNT_ID", insertable = false,
            updatable = false, nullable = false)
    private PaymentAccount paymentAccount;

    public UserAccountMapId(ProfessionalUser professionalUser, PaymentAccount paymentAccount) {

        this.professionalUser = professionalUser;
        this.paymentAccount = paymentAccount;

    }

}
