package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class PaymentAccountServiceImpl implements PaymentAccountService {

    @Autowired
    ApplicationConfiguration configuration;
    @Autowired
    UserProfileFeignClient userProfileFeignClient;

    private ProfessionalUserRepository professionalUserRepository;
    private PaymentAccountRepository paymentAccountRepository;
    private OrganisationRepository organisationRepository;
    private UserAccountMapRepository userAccountMapRepository;
    private OrganisationServiceImpl organisationServiceImpl;

    public Organisation findPaymentAccountsByEmail(String email) {

        ProfessionalUser user = professionalUserRepository.findByEmailAddress(RefDataUtil.removeAllSpaces(email));

        Organisation organisation = null;
        List<PaymentAccount> paymentAccountsEntity;

        if (null != user
                && OrganisationStatus.ACTIVE.equals(user.getOrganisation().getStatus())) {

            if ("true".equalsIgnoreCase(configuration.getPbaFromUserAccountMap())) {

                List<PaymentAccount> userMapPaymentAccount = RefDataUtil.getPaymentAccountsFromUserAccountMap(user.getUserAccountMap());
                paymentAccountsEntity = RefDataUtil
                        .getPaymentAccountFromUserMap(userMapPaymentAccount, user.getOrganisation().getPaymentAccounts());

                user.getOrganisation().setPaymentAccounts(paymentAccountsEntity);

                user.getOrganisation().setUsers(RefDataUtil.getUserIdFromUserProfile(user.getOrganisation().getUsers(), userProfileFeignClient, false));
                organisation = user.getOrganisation();

            } else if ("false".equalsIgnoreCase(configuration.getPbaFromUserAccountMap())) {

                paymentAccountsEntity = RefDataUtil.getPaymentAccount(user.getOrganisation().getPaymentAccounts());
                user.getOrganisation().setPaymentAccounts(paymentAccountsEntity);
                user.getOrganisation().setUsers(RefDataUtil.getUserIdFromUserProfile(user.getOrganisation().getUsers(), userProfileFeignClient, false));
                organisation = user.getOrganisation();
            }

        }
        return organisation;
    }

    @Transactional
    public void deleteUserAccountMaps(Organisation organisation) {
        ProfessionalUser user = organisation.getUsers().get(0).toProfessionalUser();
        List<PaymentAccount> paymentAccount = organisation.getPaymentAccounts();
        List<UserAccountMapId> accountsToDelete = generateListOfAccountsToDelete(user, paymentAccount);

        userAccountMapRepository.deleteByUserAccountMapIdIn(accountsToDelete);
    }

    @Transactional
    public void deletePaymentAccountsFromOrganisation(Organisation organisation) {
        List<UUID> accountIds = new ArrayList<>();

        organisation.getPaymentAccounts().forEach(account -> accountIds.add(account.getId()));

        paymentAccountRepository.deleteByIdIn(accountIds);

        List<PaymentAccount> emptyAccountList = new ArrayList<>();
        organisation.setPaymentAccounts(emptyAccountList);
    }


    public void addPaymentAccountsToOrganisation(PbaEditRequest pbaEditRequest, Organisation organisation) {
        organisationServiceImpl.addPbaAccountToOrganisation(pbaEditRequest.getPaymentAccounts(), organisation);
    }

    public PbaResponse addUserAndPaymentAccountsToUserAccountMap(Organisation organisation) {
        List<PaymentAccount> paymentAccounts = organisation.getPaymentAccounts();
        SuperUser superUser = organisation.getUsers().get(0);

        organisationServiceImpl.persistedUserAccountMap(superUser.toProfessionalUser(), paymentAccounts);

        return new PbaResponse(HttpStatus.OK.toString(), HttpStatus.OK.getReasonPhrase());
    }

    private List<UserAccountMapId> generateListOfAccountsToDelete(ProfessionalUser user, List<PaymentAccount> accounts) {
        List<UserAccountMapId> userAccountMapIdList = new ArrayList<>();

        accounts.forEach(account -> {
            if (null != user && null != account) {
                UserAccountMapId userAccountMapId = new UserAccountMapId(user, account);
                userAccountMapIdList.add(userAccountMapId);
            }
        });
        return userAccountMapIdList;
    }
}
