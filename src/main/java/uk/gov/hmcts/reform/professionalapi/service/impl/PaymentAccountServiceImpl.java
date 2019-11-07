package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
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

    @Override
    public PbaResponse editPaymentsAccountsByOrgId(PbaEditRequest pbaEditRequest, String orgId) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(orgId);

        if (null == organisation) {
            throw new EmptyResultDataAccessException(1);
        }

        SuperUser superUser = organisation.getUsers().get(0);
        PaymentAccount paymentAccount = organisation.getPaymentAccounts().get(0);

        UserAccountMapId userAccountMapIdFromOrg = new UserAccountMapId(superUser.toProfessionalUser(), paymentAccount);

        Optional<UserAccountMap> userAccountMap = userAccountMapRepository.findByUserAccountMapId(userAccountMapIdFromOrg);

        UserAccountMapId userAccountMapId = userAccountMap.get().getUserAccountMapId();

        deleteUserAndPaymentAccountsFromUserAccountMap(orgId);

        deletePaymentAccountsFromOrganisation(orgId);

        addPaymentAccountsToOrganisation(pbaEditRequest, orgId);

        addUserAndPaymentAccountsToUserAccountMap(orgId);

        return new PbaResponse("200", "Success");
    }


    public void deleteUserAndPaymentAccountsFromUserAccountMap(String orgId) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(orgId);

        List<UserAccountMapId> userAccountMapIdList = new ArrayList<>();
        ProfessionalUser user = organisation.getUsers().get(0).toProfessionalUser();
        List<PaymentAccount> paymentAccount = organisation.getPaymentAccounts();

        paymentAccount.stream().forEach(account -> {
            if (null != user && null != account) {
                UserAccountMapId userAccountMapId = new UserAccountMapId(user, account);
                userAccountMapIdList.add(userAccountMapId);
            }
        });
        userAccountMapRepository.deleteByUserAccountMapIdIn(userAccountMapIdList);

        //TODO remove below lines (Used to check DB queries are executing correctly)
        List<UserAccountMap> shouldBeEmptyList = userAccountMapRepository.findAll();
        shouldBeEmptyList.size();
    }

    private void deletePaymentAccountsFromOrganisation(String orgId) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(orgId);
        List<UUID> accountIds = new ArrayList<>();

        organisation.getPaymentAccounts().stream().forEach(account -> accountIds.add(account.getId()));

        //TODO remove below lines (Used to check DB queries are executing correctly)
        List<PaymentAccount> shouldNotBeEmptyList = paymentAccountRepository.findAll();
        shouldNotBeEmptyList.size();

        paymentAccountRepository.deleteByIdIn(accountIds);

        //TODO remove below lines (Used to check DB queries are executing correctly)
        List<PaymentAccount> shouldBeEmptyList = paymentAccountRepository.findAll();
        shouldBeEmptyList.size();

        List<PaymentAccount> emptyAccountList = new ArrayList<>();
        organisation.setPaymentAccounts(emptyAccountList);
    }

    private void addPaymentAccountsToOrganisation(PbaEditRequest pbaEditRequest, String orgId) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(orgId);

        organisationServiceImpl.addPbaAccountToOrganisation(pbaEditRequest.getPaymentAccounts(), organisation);
        Organisation updatedOrganisation = organisationRepository.findByOrganisationIdentifier(orgId);

        updatedOrganisation.getPaymentAccounts();
    }

    private void addUserAndPaymentAccountsToUserAccountMap(String orgId) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(orgId);

        List<PaymentAccount> paymentAccounts = organisation.getPaymentAccounts();
        SuperUser superUser = organisation.getUsers().get(0);

        organisationServiceImpl.persistedUserAccountMap(superUser.toProfessionalUser(), paymentAccounts);

        //TODO remove below lines (Used to check DB queries are executing correctly)
        List<UserAccountMap> shouldNotBeEmptyList = userAccountMapRepository.findAll();
        shouldNotBeEmptyList.size();
    }
}
