package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountMapId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "PROFESSIONAL_USER_ID",
            nullable = false)
    private ProfessionalUser professionalUser;

    @ManyToOne
    @JoinColumn(name = "PAYMENT_ACCOUNT_ID",nullable = false)
    private PaymentAccount paymentAccount;

}
