package uk.gov.hmcts.reform.professionalapi.provider;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

import java.time.LocalDateTime;
import java.util.UUID;

public final class PaymentAccountBuilder {
    private UUID id;
    private String pbaNumber;
    private Organisation organisation;
    private LocalDateTime lastUpdated;
    private LocalDateTime created;

    private PaymentAccountBuilder() {
    }

    public static PaymentAccountBuilder aPaymentAccount() {
        return new PaymentAccountBuilder();
    }

    public PaymentAccountBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public PaymentAccountBuilder withPbaNumber(String pbaNumber) {
        this.pbaNumber = pbaNumber;
        return this;
    }

    public PaymentAccountBuilder withOrganisation(Organisation organisation) {
        this.organisation = organisation;
        return this;
    }

    public PaymentAccountBuilder withLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public PaymentAccountBuilder withCreated(LocalDateTime created) {
        this.created = created;
        return this;
    }

    public PaymentAccount build() {
        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setId(id);
        paymentAccount.setPbaNumber(pbaNumber);
        paymentAccount.setOrganisation(organisation);
        paymentAccount.setLastUpdated(lastUpdated);
        paymentAccount.setCreated(created);
        return paymentAccount;
    }
}
