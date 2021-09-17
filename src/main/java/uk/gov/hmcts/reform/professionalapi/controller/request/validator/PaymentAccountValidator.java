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
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ADD_PBA_REQUEST_EMPTY;

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
            paymentAccounts.removeIf(String::isBlank);
            checkPbaNumberIsValid(paymentAccounts, true);
            checkPbasAreUniqueWithOrgId(paymentAccounts, orgId);
        }
    }

    public static String checkPbaNumberIsValid(Set<String> paymentAccounts, boolean throwException) {
        String invalidPbas = paymentAccounts.stream()
                .filter(pbaAccount -> {
                    if (!StringUtils.isBlank(pbaAccount) && pbaAccount.length() == 10) {
                        Pattern pattern = Pattern.compile("(?i)pba\\w{7}$");
                        Matcher matcher = pattern.matcher(pbaAccount);
                        if (matcher.matches()) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.joining(","));

        if (throwException && !StringUtils.isEmpty(invalidPbas)) {
            throw new InvalidRequest("PBA numbers must start with PBA/pba and be followed by 7 alphanumeric "
                    .concat("characters. The following PBAs entered are invalid: " + invalidPbas));
        } else {
            return invalidPbas;
        }
    }

    public void checkPbasAreUniqueWithOrgId(Set<String> paymentAccounts, String orgId) {
        Set<String> upperCasePbas = getUpperCasePbas(paymentAccounts);

        List<PaymentAccount> paymentAccountsInDatabase = paymentAccountRepository.findByPbaNumberIn(upperCasePbas);
        List<String> uniquePBas = new ArrayList<>();

        paymentAccountsInDatabase.forEach(pbaInDb -> upperCasePbas.forEach(pba -> {
            if (pbaInDb.getPbaNumber().equals(pba) && !pbaInDb.getOrganisation().getOrganisationIdentifier()
                    .equals(orgId)) {
                uniquePBas.add(pba);
            }
        }));

        if (!uniquePBas.isEmpty()) {
            throw new InvalidRequest("The PBA numbers you have entered: " + String.join(", ", uniquePBas)
                    .concat(" belongs to another Organisation"));
        }
    }

    public Set<String> getDuplicatePbas(Set<String> paymentAccounts) {
        Set<String> upperCasePbas = getUpperCasePbas(paymentAccounts);
        List<PaymentAccount> paymentAccountsInDatabase = paymentAccountRepository.findByPbaNumberIn(upperCasePbas);
        return paymentAccountsInDatabase.stream().map(PaymentAccount::getPbaNumber).collect(Collectors.toSet());
    }

    private Set<String> getUpperCasePbas(Set<String> paymentAccounts) {
        return paymentAccounts.stream().map(String::toUpperCase).collect(Collectors.toSet());
    }


    public void IsPbaRequestEmptyOrNull(PbaRequest pbaRequest) {
        if (pbaRequest.getPaymentAccounts() == null
                || CollectionUtils.isEmpty(pbaRequest.getPaymentAccounts())
                || pbaRequest.getPaymentAccounts().stream().allMatch(s -> (s == null || s.trim().equals("")))) {
            throw new InvalidRequest(ADD_PBA_REQUEST_EMPTY);
        }
    }
}
