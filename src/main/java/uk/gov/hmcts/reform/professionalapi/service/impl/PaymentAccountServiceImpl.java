package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
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
    private PaymentAccountRepository paymentAccountRepository;
    private OrganisationServiceImpl organisationServiceImpl;
    private UserAccountMapService userAccountMapService;

    public Organisation findPaymentAccountsByEmail(String email) {

        ProfessionalUser user = professionalUserRepository.findByEmailAddress(RefDataUtil.removeAllSpaces(email));
        Organisation organisation = null;
        List<PaymentAccount> paymentAccountsEntity;

        if (null != user && OrganisationStatus.ACTIVE.equals(user.getOrganisation().getStatus())) {

            paymentAccountsEntity = RefDataUtil.getPaymentAccount(user.getOrganisation().getPaymentAccounts());
            user.getOrganisation().setPaymentAccounts(paymentAccountsEntity);
            user.getOrganisation().setUsers(RefDataUtil.getUserIdFromUserProfile(user.getOrganisation().getUsers(),
                    userProfileFeignClient, false));
            organisation = user.getOrganisation();
        }
        return organisation;
    }

    @Override
    @Transactional
    public PbaResponse editPaymentAccountsByOrganisation(Organisation organisation, PbaEditRequest pbaEditRequest) {
        deleteUserAccountMaps(organisation);
        deletePaymentAccountsFromOrganisation(organisation);
        paymentAccountRepository.flush();
        addPaymentAccountsToOrganisation(pbaEditRequest,organisation);
        return addUserAndPaymentAccountsToUserAccountMap(organisation);
    }

    public void deleteUserAccountMaps(Organisation organisation) {
        /** Please note:
         * Currently only the Super User of an Organisation is linked to the Payment Accounts via the User Account Map.
         * If this changes then the below logic will need to change accordingly */
        ProfessionalUser user = organisation.getUsers().get(0).toProfessionalUser();

        List<PaymentAccount> paymentAccount = organisation.getPaymentAccounts();
        List<UserAccountMapId> accountsToDelete = generateListOfAccountsToDelete(user, paymentAccount);

        userAccountMapService.deleteByUserAccountMapIdIn(accountsToDelete);
    }


    public void deletePaymentAccountsFromOrganisation(Organisation organisation) {
        List<UUID> accountIds = new ArrayList<>();

        organisation.getPaymentAccounts().forEach(account -> accountIds.add(account.getId()));

        paymentAccountRepository.deleteByIdIn(accountIds);

        /** Please Note:
         * The below lines are required to set the Organisation's Payment Accounts List to be empty.
         * If this is not done, the Organisation's list will still contain the previous Accounts */
        List<PaymentAccount> resetOrganisationPaymentAccounts = new ArrayList<>();
        organisation.setPaymentAccounts(resetOrganisationPaymentAccounts);
    }

    public void addPaymentAccountsToOrganisation(PbaEditRequest pbaEditRequest, Organisation organisation) {
        organisationServiceImpl.addPbaAccountToOrganisation(pbaEditRequest.getPaymentAccounts(), organisation);
    }

    public PbaResponse addUserAndPaymentAccountsToUserAccountMap(Organisation organisation) {

        List<PaymentAccount> paymentAccounts = organisation.getPaymentAccounts();
        SuperUser superUser = organisation.getUsers().get(0);

        /** Please note:
         * Currently only the Super User of an Organisation is linked to the Payment Accounts via the User Account Map.
         * If this changes then the below logic will need to change accordingly */
        userAccountMapService.persistedUserAccountMap(superUser.toProfessionalUser(), paymentAccounts);
        return new PbaResponse(HttpStatus.OK.toString(), HttpStatus.OK.getReasonPhrase());
    }

    public List<UserAccountMapId> generateListOfAccountsToDelete(ProfessionalUser user, List<PaymentAccount> accounts) {

        return accounts.stream().filter(account -> null != user && null != account)
                .map(account -> new UserAccountMapId(user, account))
                .collect(Collectors.toList());
    }
}