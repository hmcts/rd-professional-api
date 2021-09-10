package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.PbaUpdateStatusResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UpdatePbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PARTIAL_SUCCESS_UPDATE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PBA_INVALID_FORMAT;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PBA_MISSING;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PBA_NOT_IN_ORG;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PBA_NOT_PENDING;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_STATUS_INVALID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_STATUS_MISSING;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE_ACCEPTED_BY_ADMIN;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator.isPbaValid;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.REJECTED;

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
    private PaymentAccountRepository paymentAccountRepository;

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

    public void addPaymentAccountsToOrganisation(PbaEditRequest pbaEditRequest, Organisation organisation) {
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

    public UpdatePbaStatusResponse updatePaymentAccountsforAnOrganisation(
            List<PbaRequest> pbaRequestList, String orgId) {

        Set<String> pbasFromRequest = new HashSet<>();

        //Get PBAs from request
        pbaRequestList.forEach(pba -> pbasFromRequest.add(pba.getPbaNumber()));

        //Generate Invalid PBA Responses from Request
        List<PbaUpdateStatusResponse> invalidPbaResponses =
                getInvalidPbaRequests(pbaRequestList, pbasFromRequest, orgId);

        //Remove invalid PBAs from PBAs to update
        invalidPbaResponses.forEach(pba -> pbasFromRequest.remove(pba.getPbaNumber()));

        //Update valid PBAs, returns any remaining invalid PBAs
        if (isNotEmpty(pbasFromRequest)) {
            invalidPbaResponses = acceptOrRejectPbas(pbasFromRequest, pbaRequestList, invalidPbaResponses);
        }

        return generateUpdatePbaResponse(invalidPbaResponses, pbasFromRequest);
    }

    private List<PbaUpdateStatusResponse> getInvalidPbaRequests(
            List<PbaRequest> pbaRequestList, Set<String> pbasFromRequest, String orgId) {

        List<PbaUpdateStatusResponse> invalidPbaRequests = new ArrayList<>();

        pbaRequestList.forEach(pba -> {
            //check pba is present
            if (isNullOrEmpty(pba.getPbaNumber())) {
                invalidPbaRequests.add(new PbaUpdateStatusResponse("", ERROR_MSG_PBA_MISSING));
                //check status is present
            } else if (isNullOrEmpty(pba.getStatus())) {
                invalidPbaRequests.add(new PbaUpdateStatusResponse(pba.getPbaNumber(), ERROR_MSG_STATUS_MISSING));
                pbasFromRequest.remove(pba.getPbaNumber());
                //check status is valid
            } else if (!ACCEPTED.name().equalsIgnoreCase(pba.getStatus())
                    && !REJECTED.name().equalsIgnoreCase(pba.getStatus())) {
                invalidPbaRequests.add(new PbaUpdateStatusResponse(pba.getPbaNumber(), ERROR_MSG_STATUS_INVALID));
                pbasFromRequest.remove(pba.getPbaNumber());
            }
        });

        //Check individual PBA numbers are valid and exist within given organisation
        invalidPbaRequests.addAll(getInvalidPbas(pbasFromRequest, orgId));

        return invalidPbaRequests;
    }

    public List<PbaUpdateStatusResponse> getInvalidPbas(Set<String> pbasFromRequest, String orgId) {
        List<PbaUpdateStatusResponse> pbaUpdateStatusResponses = new ArrayList<>();

        pbasFromRequest.forEach(pba -> {
            //check PBA has a valid format
            if (isPbaValid(pba)) {
                new PbaUpdateStatusResponse(pba, ERROR_MSG_PBA_INVALID_FORMAT);
            }

            Optional<PaymentAccount> paymentAccount = paymentAccountRepository.findByPbaNumber(pba);

            //check PBA exists in DB
            if (paymentAccount.isPresent()) {
                //check PBA belongs to the given Organisation
                if (!paymentAccount.get().getOrganisation().getOrganisationIdentifier().equals(orgId)) {
                    pbaUpdateStatusResponses.add(new PbaUpdateStatusResponse(pba, ERROR_MSG_PBA_NOT_IN_ORG));
                }
            } else {
                pbaUpdateStatusResponses.add(new PbaUpdateStatusResponse(pba, ERROR_MSG_PBA_NOT_IN_ORG));
            }
        });

        return pbaUpdateStatusResponses;
    }

    public List<PbaUpdateStatusResponse> acceptOrRejectPbas(
            Set<String> pbasFromRequest, List<PbaRequest> pbaRequestList, List<PbaUpdateStatusResponse> invalidPbas) {

        pbasFromRequest.forEach(pba -> pbaRequestList.forEach(pba1 -> {
            if (pba1.getPbaNumber().equals(pba)) {
                Optional<PaymentAccount> paymentAccount = paymentAccountRepository.findByPbaNumber(pba);
                //if PBA status is ACCEPTED update status and statusMessage in DB
                if (pba1.getStatus().equals(ACCEPTED.name())) {
                    //check PBA's current status is NOT ACCEPTED before updating to ACCEPTED
                    if (!ACCEPTED.equals(paymentAccount.get().getPbaStatus())) {
                        paymentAccount.get().setPbaStatus(ACCEPTED);
                        paymentAccount.get().setStatusMessage(PBA_STATUS_MESSAGE_ACCEPTED_BY_ADMIN);
                        paymentAccountRepository.save(paymentAccount.get());
                    } else {
                        invalidPbas.add(new PbaUpdateStatusResponse(pba, ERROR_MSG_PBA_NOT_PENDING));
                    }
                    //if REJECTED delete from DB
                } else if (pba1.getStatus().equals(REJECTED.name())) {
                    paymentAccountRepository.delete(paymentAccount.get());
                }
            }
        }));

        return invalidPbas;
    }

    public UpdatePbaStatusResponse generateUpdatePbaResponse(
            List<PbaUpdateStatusResponse> invalidPbaResponses, Set<String> pbasFromRequest) {

        if (isNotEmpty(invalidPbaResponses) && isEmpty(pbasFromRequest)) {
            return new UpdatePbaStatusResponse(null, invalidPbaResponses, 422);

        } else if (isNotEmpty(invalidPbaResponses) && isNotEmpty(pbasFromRequest)) {
            return new UpdatePbaStatusResponse(ERROR_MSG_PARTIAL_SUCCESS_UPDATE, invalidPbaResponses, 200);
        }

        return new UpdatePbaStatusResponse(null, null, 200);
    }
}
