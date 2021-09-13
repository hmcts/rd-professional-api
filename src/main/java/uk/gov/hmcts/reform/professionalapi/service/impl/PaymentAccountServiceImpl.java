package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentAccountServiceImpl implements PaymentAccountService {

    @Autowired
    ApplicationConfiguration configuration;
    @Autowired
    UserProfileFeignClient userProfileFeignClient;
    @Autowired
    EntityManagerFactory emf;

    private ProfessionalUserRepository professionalUserRepository;
    private OrganisationService organisationService;
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
    public PbaResponse editPaymentAccountsByOrganisation(Organisation organisation, PbaRequest pbaEditRequest) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        deleteUserAccountMapsAndPaymentAccounts(em, organisation);
        addPaymentAccountsToOrganisation(pbaEditRequest, organisation);
        PbaResponse pbaResponse = addUserAndPaymentAccountsToUserAccountMap(organisation);

        transaction.commit();
        em.close();
        return pbaResponse;
    }

    @Override
    public void deletePaymentsOfOrganisation(PbaRequest deletePbaRequest, Organisation organisation) {
        organisationService.deletePaymentsOfOrganisation(deletePbaRequest.getPaymentAccounts(), organisation);
    }

    public void deleteUserAccountMapsAndPaymentAccounts(EntityManager em, Organisation organisation) {
        List<PaymentAccount> paymentAccount = organisation.getPaymentAccounts();

        /** Please note:
         * Currently only the Super User of an Organisation is linked to the Payment Accounts via the User Account Map.
         * If this changes then the below method will need to change accordingly */
        removePaymentAccountAndUserAccountMaps(em,
                generateListOfAccountsToDelete(organisation.getUsers().get(0).toProfessionalUser(), paymentAccount));

        /** Please Note:
         * The below lines are required to set the Organisation's Payment Accounts List to be empty.
         * If this is not done, the Organisation's list will still contain the previous Accounts */
        organisation.setPaymentAccounts(new ArrayList<>());
    }

    public void addPaymentAccountsToOrganisation(PbaRequest pbaEditRequest, Organisation organisation) {
        organisationService.addPbaAccountToOrganisation(pbaEditRequest.getPaymentAccounts(), organisation, true, true);
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

    public void removePaymentAccountAndUserAccountMaps(EntityManager em, List<UserAccountMapId> userAccountMaps) {
        userAccountMaps.forEach(userAccountMap -> {
            em.remove(em.find(UserAccountMap.class, userAccountMap));
            em.remove(em.find(PaymentAccount.class, userAccountMap.getPaymentAccount().getId()));
        });

    }
}