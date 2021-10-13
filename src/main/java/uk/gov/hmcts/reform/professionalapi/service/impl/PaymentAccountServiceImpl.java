package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaUpdateRequest;
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
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator.isPbaInvalid;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;
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

    public UpdatePbaStatusResponse updatePaymentAccountsStatusForAnOrganisation(
            List<PbaUpdateRequest> pbaRequestList, String orgId) {

        Set<String> pbasFromRequest = new HashSet<>();
        List<PaymentAccount> pbasFromDb = new ArrayList<>();

        //Get PBAs from request
        pbaRequestList.forEach(pba -> pbasFromRequest.add(pba.getPbaNumber()));

        //Generate Invalid PBA Responses from Request
        List<PbaUpdateStatusResponse> invalidPbaResponses =
                getInvalidPbaRequests(pbaRequestList, pbasFromRequest);

        //Remove invalid PBAs from PBAs to update
        invalidPbaResponses.forEach(pba -> pbasFromRequest.remove(pba.getPbaNumber()));

        //Remove any null references caused by any null PBA values that may be present in request
        pbasFromRequest.remove(null);

        //get PBAs from DB and remove invalid PBAs & PBAs that don't exist in DB from PBAs to update
        if (isNotEmpty(pbasFromRequest)) {
            pbasFromDb = paymentAccountRepository.findByPbaNumberIn(pbasFromRequest);
            invalidPbaResponses = getInvalidPbasFromDb(
                    pbasFromDb, pbasFromRequest, invalidPbaResponses, orgId);
            invalidPbaResponses.forEach(pba -> pbasFromRequest.remove(pba.getPbaNumber()));
        }

        //Update valid PBAs, returns any remaining invalid PBAs
        if (isNotEmpty(pbasFromRequest)) {
            invalidPbaResponses = acceptOrRejectPbas(pbasFromDb, pbaRequestList, invalidPbaResponses);
            invalidPbaResponses.forEach(pba -> pbasFromRequest.remove(pba.getPbaNumber()));
        }

        return generateUpdatePbaResponse(invalidPbaResponses, pbasFromRequest);
    }

    private List<PbaUpdateStatusResponse> getInvalidPbaRequests(
            List<PbaUpdateRequest> pbaRequestList, Set<String> pbasFromRequest) {

        List<PbaUpdateStatusResponse> invalidPbaResponses = new ArrayList<>();

        pbaRequestList.forEach(pba -> {
            //check pba is present
            if (isNullOrEmpty(pba.getPbaNumber())) {
                invalidPbaResponses.add(generateInvalidResponse("", ERROR_MSG_PBA_MISSING));
                //check pba is invalid
            } else if (isPbaInvalid(pba.getPbaNumber())) {
                invalidPbaResponses.add(generateInvalidResponse(pba.getPbaNumber(), ERROR_MSG_PBA_INVALID_FORMAT));
                pbasFromRequest.remove(pba.getPbaNumber());
                //check status is present
            } else if (isNullOrEmpty(pba.getStatus())) {
                invalidPbaResponses.add(generateInvalidResponse(pba.getPbaNumber(), ERROR_MSG_STATUS_MISSING));
                pbasFromRequest.remove(pba.getPbaNumber());
                //check status is valid
            } else if (!ACCEPTED.name().equalsIgnoreCase(pba.getStatus())
                    && !REJECTED.name().equalsIgnoreCase(pba.getStatus())) {
                invalidPbaResponses.add(generateInvalidResponse(pba.getPbaNumber(), ERROR_MSG_STATUS_INVALID));
                pbasFromRequest.remove(pba.getPbaNumber());
            }
        });

        return invalidPbaResponses;
    }

    public List<PbaUpdateStatusResponse> getInvalidPbasFromDb(
            List<PaymentAccount> pbasFromDb, Set<String> pbasFromRequest,
            List<PbaUpdateStatusResponse> invalidPbaResponses, String orgId) {

        //if list of PBAs from DB is empty, add invalid PBAs to response
        if (isEmpty(pbasFromDb)) {
            pbasFromRequest.forEach(pba ->
                    invalidPbaResponses.add(generateInvalidResponse(pba, ERROR_MSG_PBA_NOT_IN_ORG)));
        } else {
            pbasFromDb.forEach(pba -> {
                //check PBA belongs to the given Organisation
                if (!pba.getOrganisation().getOrganisationIdentifier().equals(orgId)) {
                    invalidPbaResponses.add(generateInvalidResponse(pba.getPbaNumber(), ERROR_MSG_PBA_NOT_IN_ORG));
                }
            });

            //get PBA Numbers from Payment Accounts
            List<String> pbaNumbersFromDb =
                    pbasFromDb.stream().map(PaymentAccount::getPbaNumber).collect(Collectors.toList());

            //Get the PBA Numbers that are not present in DB
            List<String> pbasNotInDb =
                    pbasFromRequest.stream().filter(pba -> !pbaNumbersFromDb.contains(pba))
                            .collect(Collectors.toList());

            //Generate invalid responses for PBAs not in DB
            pbasNotInDb.forEach(invalidPba -> invalidPbaResponses.add(
                    generateInvalidResponse(invalidPba, ERROR_MSG_PBA_NOT_IN_ORG)));
        }

        return invalidPbaResponses;
    }

    public List<PbaUpdateStatusResponse> acceptOrRejectPbas(
            List<PaymentAccount> pbasFromDb, List<PbaUpdateRequest> pbaRequestList,
            List<PbaUpdateStatusResponse> invalidPbaResponses) {

        List<PaymentAccount> pbasToSave = new ArrayList<>();
        List<PaymentAccount> pbasToDelete = new ArrayList<>();

        pbasFromDb.forEach(pba -> pbaRequestList.forEach(pba1 -> {
            if (null != pba1.getPbaNumber() && pba1.getPbaNumber().equals(pba.getPbaNumber())) {
                //if PBA status is ACCEPTED update status and statusMessage in DB
                if (pba1.getStatus().equalsIgnoreCase(ACCEPTED.name())) {
                    //check PBA's current status is NOT ACCEPTED before updating to ACCEPTED
                    if (PENDING.equals(pba.getPbaStatus())) {
                        pba.setPbaStatus(ACCEPTED);
                        pba.setStatusMessage(getStatusMessageFromRequest(pba1));
                        pbasToSave.add(pba);
                    } else {
                        invalidPbaResponses.add(generateInvalidResponse(pba.getPbaNumber(), ERROR_MSG_PBA_NOT_PENDING));
                    }
                    //if REJECTED delete from DB
                } else if (pba1.getStatus().equalsIgnoreCase(REJECTED.name())) {
                    pbasToDelete.add(pba);
                }
            }

        }));

        updatePBAsInDb(pbasToSave, pbasToDelete);

        return invalidPbaResponses;
    }

    public void updatePBAsInDb(List<PaymentAccount> pbasToSave, List<PaymentAccount> pbasToDelete) {
        if (isNotEmpty(pbasToSave)) {
            paymentAccountRepository.saveAll(pbasToSave);
        }

        if (isNotEmpty(pbasToDelete)) {
            paymentAccountRepository.deleteAll(pbasToDelete);
        }
    }

    public PbaUpdateStatusResponse generateInvalidResponse(String pbaNumber, String errorMessage) {
        return new PbaUpdateStatusResponse(pbaNumber, errorMessage);
    }

    public UpdatePbaStatusResponse generateUpdatePbaResponse(
            List<PbaUpdateStatusResponse> invalidPbaResponses, Set<String> pbasFromRequest) {

        //if there are only invalid PBAs
        if (isNotEmpty(invalidPbaResponses) && isEmpty(pbasFromRequest)) {
            return new UpdatePbaStatusResponse(
                    null, invalidPbaResponses, HttpStatus.UNPROCESSABLE_ENTITY.value());
            //if there are some invalid PBAs and some valid PBAs that have been updated
        } else if (isNotEmpty(invalidPbaResponses) && isNotEmpty(pbasFromRequest)) {
            return new UpdatePbaStatusResponse(
                    ERROR_MSG_PARTIAL_SUCCESS_UPDATE, invalidPbaResponses, HttpStatus.OK.value());
        }
        //if there are only valid PBAs that have been updated
        return new UpdatePbaStatusResponse(
                null, null, HttpStatus.OK.value());
    }

    public String getStatusMessageFromRequest(PbaUpdateRequest pba) {
        return pba.getStatusMessage() == null ? "" : pba.getStatusMessage();
    }
}
