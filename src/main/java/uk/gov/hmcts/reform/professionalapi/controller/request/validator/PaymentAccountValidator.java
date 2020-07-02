package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;

@Component
@Slf4j
@NoArgsConstructor
public class PaymentAccountValidator {

    PaymentAccountRepository paymentAccountRepository;

    @Autowired
    public PaymentAccountValidator(PaymentAccountRepository paymentAccountRepository) {
        this.paymentAccountRepository = paymentAccountRepository;
    }

    public void validatePaymentAccounts(Set<String> paymentAccounts, String orgId) {
        if (!CollectionUtils.isEmpty(paymentAccounts)) {
            checkPbaNumberIsValid(paymentAccounts);
            checkPbasAreUniqueWithOrgId(paymentAccounts, orgId);
        }
    }

    public static void checkPbaNumberIsValid(Set<String> paymentAccounts) {
        String invalidPbas = paymentAccounts.stream()
                .filter(pbaAccount -> {
                    if (!StringUtils.isBlank(pbaAccount) && pbaAccount.length() == 10) {
                        Pattern pattern = Pattern.compile("(?i)pba.[a-zA-Z0-9]*$");
                        Matcher matcher = pattern.matcher(pbaAccount);
                        if (matcher.matches()) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.joining(", "));

        if (!StringUtils.isEmpty(invalidPbas)) {
            throw new InvalidRequest("PBA numbers must start with PBA/pba and be followed by 7 alphanumeric characters. The following PBAs entered are invalid: " + invalidPbas);
        }
    }


    public void checkPbasAreUniqueWithOrgId(Set<String> paymentAccounts, String orgId) {
        Set<String> upperCasePbas = paymentAccounts.stream().map(String::toUpperCase).collect(Collectors.toSet());

        List<PaymentAccount> paymentAccountsInDatabase = paymentAccountRepository.findByPbaNumberIn(upperCasePbas);
        List<String> uniquePBas = new ArrayList<>();

        paymentAccountsInDatabase.forEach(pbaInDb -> upperCasePbas.forEach(pba -> {
            if (pbaInDb.getPbaNumber().equals(pba) && !pbaInDb.getOrganisation().getOrganisationIdentifier().equals(orgId)) {
                uniquePBas.add(pba);
            }
        }));

        if (!uniquePBas.isEmpty()) {
            throw new InvalidRequest("The PBA numbers you have entered: " + String.join(", ", uniquePBas) + " belongs to another Organisation");
        }
    }

}
