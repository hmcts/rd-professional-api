package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentAccountServiceImpl implements PaymentAccountService {

    private ProfessionalUserRepository professionalUserRepository;

    public Organisation findPaymentAccountsByEmail(String email) {

        ProfessionalUser user = professionalUserRepository.findByEmailAddress(email);

        Organisation organisation = null;
        List<PaymentAccount> paymentAccountsEntity;

        if (null != user && OrganisationStatus.ACTIVE.equals(user.getOrganisation().getStatus())) {

            List<PaymentAccount> userMapPaymentAccount =  PbaAccountUtil.getPaymentAccountsFromUserAccountMap(user.getUserAccountMap());
            paymentAccountsEntity = PbaAccountUtil
                    .getPaymentAccountFromUserMap(userMapPaymentAccount, user.getOrganisation().getPaymentAccounts());

            user.getOrganisation().setPaymentAccounts(paymentAccountsEntity);
            organisation = user.getOrganisation();
        }
        return organisation;
    }
}
