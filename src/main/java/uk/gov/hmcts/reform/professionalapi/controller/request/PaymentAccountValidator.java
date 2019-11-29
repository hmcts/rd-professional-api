package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;

@Component
@Slf4j
public class PaymentAccountValidator {

    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    public void validatePaymentAccounts(Set<String> paymentAccounts, String orgId) {
        if (!paymentAccounts.isEmpty()) {
            checkPbaNumberIsValid(paymentAccounts);
            checkPbasAreUniqueWithOrgId(paymentAccounts, orgId);
        }
    }

    public void checkPbaNumberIsValid(Set<String> paymentAccounts) {
        String invalidPbas = paymentAccounts.stream()
                .filter(pbaAccount -> pbaAccount == null || !pbaAccount.matches("^(PBA|pba)[a-zA-Z0-9]*$"))
                .collect(Collectors.joining(", "));

        if (!StringUtils.isEmpty(invalidPbas)) {
            throw new InvalidRequest("PBA numbers must start with PBA/pba and be followed by 7 alphanumeric characters. The following PBAs entered are invalid: " + invalidPbas);
        }
    }

    public void checkPbasAreUniqueWithOrgId(Set<String> paymentAccounts, String orgId) {
        List<PaymentAccount> paymentAccountsInDatabase = paymentAccountRepository.findByPbaNumberIn(paymentAccounts);
        List<String> uniquePBas = new ArrayList<>();

        paymentAccountsInDatabase.forEach(pbaInDb -> paymentAccounts.forEach(pba -> {
            if (pbaInDb.getPbaNumber().equals(pba) && !pbaInDb.getOrganisation().getOrganisationIdentifier().equals(orgId)) {
                uniquePBas.add(pba);
            }
        }));

        if (!uniquePBas.isEmpty()) {
            throw new InvalidRequest("The PBA numbers you have entered: " + String.join(", ", uniquePBas) + " belongs to another Organisation");
        }
    }

}
