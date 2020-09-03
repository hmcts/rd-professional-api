package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.service.LegacyPbaAccountService;

@Service
@Slf4j
public class LegacyPbaAccountServiceImpl implements LegacyPbaAccountService {

    @Autowired
    ApplicationConfiguration config;

    public List<String> findLegacyPbaAccountByUserEmail(ProfessionalUser professionalUser) {
        List<String> pbaNumbers = null;

        if (!professionalUser.getOrganisation().getPaymentAccounts().isEmpty()) {
            pbaNumbers = getPbaNumbersFromPaymentAccount(professionalUser.getOrganisation().getPaymentAccounts());
        }
        return pbaNumbers;
    }

    private List<String> getPbaNumbersFromPaymentAccount(List<PaymentAccount> paymentAccountsEntity) {

        List<String> paymentAccountPbaNumbers = new ArrayList<>();

        paymentAccountsEntity.forEach(paymentAccount ->
                paymentAccountPbaNumbers.add(paymentAccount.getPbaNumber().trim())
        );

        return paymentAccountPbaNumbers;
    }
}
