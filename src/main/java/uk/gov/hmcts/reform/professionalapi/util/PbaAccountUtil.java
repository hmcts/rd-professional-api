package uk.gov.hmcts.reform.professionalapi.util;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;

@Slf4j
public class PbaAccountUtil {

    public static List<PaymentAccount> getPaymentAccountsFromUserAccountMap(List<UserAccountMap> userAccountMaps) {

        List<PaymentAccount> userMapPaymentAccount = new ArrayList<>();

        userMapPaymentAccount = userAccountMaps.stream().map(
            userAccountMap -> userAccountMap.getUserAccountMapId().getPaymentAccount())
                .collect(toList());

        return userMapPaymentAccount;
    }


    public static List<PaymentAccount> getPaymentAccountFromUserMap(List<PaymentAccount> userMapPaymentAccount, List<PaymentAccount> paymentAccountsEntity) {

        List<PaymentAccount> paymentAccounts = new ArrayList<>();

        if (!paymentAccountsEntity.isEmpty()) {

            paymentAccountsEntity.forEach(paymentAccount -> {
                for (PaymentAccount usrMapAccount : userMapPaymentAccount) {
                    if (usrMapAccount.getId().equals(paymentAccount.getId())) {

                        paymentAccounts.add(paymentAccount);

                    }
                }
            });
        }
        return paymentAccounts;
    }

}
