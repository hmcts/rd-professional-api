package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdatePbaRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;

import static org.springframework.util.CollectionUtils.isEmpty;
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

    public void validatePaymentAccounts(Set<String> paymentAccounts, Organisation org, boolean isDeletePba) {
        if (!CollectionUtils.isEmpty(paymentAccounts)) {
            paymentAccounts.removeIf(String::isBlank);
            checkPbaNumberIsValid(paymentAccounts, true);
            if (isDeletePba) {
                checkPbasBelongToOrganisation(paymentAccounts, org);
            } else {
                checkPbasAreUniqueWithOrgId(paymentAccounts, org);
            }
        }
    }

    public static String checkPbaNumberIsValid(Set<String> paymentAccounts, boolean throwException) {
        String invalidPbas = paymentAccounts.stream()
                .filter(PaymentAccountValidator::isPbaInvalid)
                .collect(Collectors.joining(","));

        if (throwException && !StringUtils.isEmpty(invalidPbas)) {
            throw new InvalidRequest("PBA numbers must start with PBA/pba and be followed by 7 alphanumeric "
                    .concat("characters. The following PBAs entered are invalid: " + invalidPbas));
        } else {
            return invalidPbas;
        }
    }

    public static boolean isPbaInvalid(String pbaAccount) {
        if (!StringUtils.isBlank(pbaAccount) && pbaAccount.length() == 10) {
            Pattern pattern = Pattern.compile("(?i)pba\\w{7}$");
            Matcher matcher = pattern.matcher(pbaAccount);
            if (matcher.matches()) {
                return false;
            }
        }
        return true;
    }

    public void checkPbasAreUniqueWithOrgId(Set<String> paymentAccounts, Organisation org) {
        Set<String> upperCasePbas = getUpperCasePbas(paymentAccounts);

        List<PaymentAccount> paymentAccountsInDatabase = paymentAccountRepository.findByPbaNumberIn(upperCasePbas);
        List<String> uniquePBas = new ArrayList<>();

        paymentAccountsInDatabase.forEach(pbaInDb -> upperCasePbas.forEach(pba -> {
            if (pbaInDb.getPbaNumber().equals(pba) && !pbaInDb.getOrganisation().getOrganisationIdentifier()
                    .equals(org.getOrganisationIdentifier())) {
                uniquePBas.add(pba);
            }
        }));

        if (!uniquePBas.isEmpty()) {
            throw new InvalidRequest("The PBA numbers you have entered: " + String.join(", ", uniquePBas)
                    .concat(" belongs to another Organisation"));
        }
    }

    public void checkPbasBelongToOrganisation(Set<String> paymentAccounts, Organisation org) {
        Set<String> upperCasePbas = getUpperCasePbas(paymentAccounts);
        List<String> orgPbas = new ArrayList<>();

        org.getPaymentAccounts().forEach(pba -> orgPbas.add(pba.getPbaNumber()));

        List<String> nonOrgPbas = new ArrayList<>();

        upperCasePbas.forEach(pba -> {
            if (!orgPbas.contains(pba)) {
                nonOrgPbas.add(pba);
            }
        });

        if (!nonOrgPbas.isEmpty()) {
            throw new InvalidRequest("The PBA numbers you have entered: " + String.join(", ", nonOrgPbas)
                    .concat(" do not belong to this Organisation"));
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


    public void isPbaRequestEmptyOrNull(PbaRequest pbaRequest) {
        if (pbaRequest.getPaymentAccounts() == null
                || CollectionUtils.isEmpty(pbaRequest.getPaymentAccounts())
                || pbaRequest.getPaymentAccounts().stream().allMatch(s -> (s == null || s.trim().equals("")))) {
            throw new InvalidRequest(ADD_PBA_REQUEST_EMPTY);
        }
    }

    public void checkUpdatePbaRequestIsValid(UpdatePbaRequest updatePbaRequest) {
        if (isEmpty(updatePbaRequest.getPbaRequestList())) {
            throw new InvalidRequest("No PBAs have been sent in the request");
        } else if (updatePbaRequest.getPbaRequestList().stream().anyMatch(Objects::isNull)) {
            throw new InvalidRequest("null values not allowed in Update PBA Request");
        }
    }

}

