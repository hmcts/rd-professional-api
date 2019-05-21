package uk.gov.hmcts.reform.professionalapi.domain;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Embeddable
@Getter
@Setter
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

    @Override
    public boolean equals(Object o) {

        if (this == o) {

            return true;
        }
        if (!(o instanceof UserAccountMapId))  {

            return false;
        }
        UserAccountMapId that = (UserAccountMapId) o;
        return Objects.equals(getProfessionalUser(), that.getProfessionalUser())
                &&  Objects.equals(getPaymentAccount(), that.getPaymentAccount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProfessionalUser(), getPaymentAccount());
    }
}
