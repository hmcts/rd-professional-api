package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentAccountServiceImpl implements PaymentAccountService {

    @Autowired
    ApplicationConfiguration configuration;
    @Autowired
    UserProfileFeignClient userProfileFeignClient;

    private ProfessionalUserRepository professionalUserRepository;

    public Organisation findPaymentAccountsByEmail(String email) {

        ProfessionalUser user = professionalUserRepository.findByEmailAddress(RefDataUtil.removeAllSpaces(email));

        Organisation organisation = null;
        List<PaymentAccount> paymentAccountsEntity;

        if (null != user
                && OrganisationStatus.ACTIVE.equals(user.getOrganisation().getStatus())) {

            if ("true".equalsIgnoreCase(configuration.getPbaFromUserAccountMap())) {

                List<PaymentAccount> userMapPaymentAccount =  RefDataUtil.getPaymentAccountsFromUserAccountMap(user.getUserAccountMap());
                paymentAccountsEntity = RefDataUtil
                        .getPaymentAccountFromUserMap(userMapPaymentAccount, user.getOrganisation().getPaymentAccounts());

                user.getOrganisation().setPaymentAccounts(paymentAccountsEntity);

                user.getOrganisation().setUsers(RefDataUtil.getUserIdFromUserProfile(user.getOrganisation().getUsers(), userProfileFeignClient, false));
                organisation = user.getOrganisation();

            } else if ("false".equalsIgnoreCase(configuration.getPbaFromUserAccountMap())) {

                paymentAccountsEntity =  RefDataUtil.getPaymentAccount(user.getOrganisation().getPaymentAccounts());
                user.getOrganisation().setPaymentAccounts(paymentAccountsEntity);
                user.getOrganisation().setUsers(RefDataUtil.getUserIdFromUserProfile(user.getOrganisation().getUsers(), userProfileFeignClient, false));
                organisation = user.getOrganisation();
            }

        }
        return organisation;
    }
}
