package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE_ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ONE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE_AUTO_ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ZERO_INDEX;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.NO_ORG_FOUND_FOR_GIVEN_ID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NOT_ACTIVE_NO_USERS_RETURNED;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Arrays;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.tuple.Pair;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaAddRequest;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.domain.AddPbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.FailedPbaReason;

@Service
@Slf4j
@Setter
public class OrganisationServiceImpl implements OrganisationService {
    @Autowired
    OrganisationRepository organisationRepository;
    @Autowired
    ProfessionalUserRepository professionalUserRepository;
    @Autowired
    PaymentAccountRepository paymentAccountRepository;
    @Autowired
    DxAddressRepository dxAddressRepository;
    @Autowired
    ContactInformationRepository contactInformationRepository;
    @Autowired
    PrdEnumRepository prdEnumRepository;
    @Autowired
    UserAccountMapService userAccountMapService;
    @Autowired
    UserProfileFeignClient userProfileFeignClient;
    @Autowired
    PrdEnumService prdEnumService;
    @Autowired
    UserAttributeService userAttributeService;
    @Autowired
    PaymentAccountValidator paymentAccountValidator;
    @Autowired
    OrganisationMfaStatusRepository organisationMfaStatusRepository;
    @Autowired
    ProfessionalUserService professionalUserService;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Override
    @Transactional
    public OrganisationResponse createOrganisationFrom(
            OrganisationCreationRequest organisationCreationRequest) {

        Organisation newOrganisation = new Organisation(
                RefDataUtil.removeEmptySpaces(organisationCreationRequest.getName()),
                OrganisationStatus.PENDING,
                RefDataUtil.removeEmptySpaces(organisationCreationRequest.getSraId()),
                RefDataUtil.removeEmptySpaces(organisationCreationRequest.getCompanyNumber()),
                Boolean.parseBoolean(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getSraRegulated()
                        .toLowerCase())),
                RefDataUtil.removeAllSpaces(organisationCreationRequest.getCompanyUrl())
        );

        Organisation organisation = saveOrganisation(newOrganisation);

        addDefaultMfaStatusToOrganisation(organisation);

        addPbaAccountToOrganisation(organisationCreationRequest.getPaymentAccount(), organisation, false, false);

        addSuperUserToOrganisation(organisationCreationRequest.getSuperUser(), organisation);

        addContactInformationToOrganisation(organisationCreationRequest.getContactInformation(), organisation);

        return new OrganisationResponse(organisation);
    }

    public Organisation saveOrganisation(Organisation organisation) {
        Organisation persistedOrganisation = null;
        try {
            persistedOrganisation = organisationRepository.save(organisation);
        } catch (ConstraintViolationException ex) {
            organisation.setOrganisationIdentifier(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));
            persistedOrganisation = organisationRepository.save(organisation);
        }
        return persistedOrganisation;
    }

    public void addDefaultMfaStatusToOrganisation(Organisation organisation) {

        OrganisationMfaStatus organisationMfaStatus = new OrganisationMfaStatus();
        organisationMfaStatus.setOrganisation(organisation);

        OrganisationMfaStatus persistedOrganisationMfaStatus
                = organisationMfaStatusRepository.save(organisationMfaStatus);
        organisation.setOrganisationMfaStatus(persistedOrganisationMfaStatus);

    }

    public void addPbaAccountToOrganisation(Set<String> paymentAccounts,
                                            Organisation organisation, boolean pbasValidated, boolean isEditPba) {
        if (paymentAccounts != null) {
            if (!pbasValidated) {
                PaymentAccountValidator.checkPbaNumberIsValid(paymentAccounts, true);
            }

            paymentAccounts.forEach(pbaAccount -> {
                PaymentAccount paymentAccount = new PaymentAccount(pbaAccount.toUpperCase());
                paymentAccount.setOrganisation(organisation);
                if (isEditPba) {
                    updateStatusAndMessage(paymentAccount, ACCEPTED, PBA_STATUS_MESSAGE_ACCEPTED);
                }
                PaymentAccount persistedPaymentAccount = paymentAccountRepository.save(paymentAccount);
                organisation.addPaymentAccount(persistedPaymentAccount);
            });
        }
    }

    public void updateStatusAndMessage(PaymentAccount paymentAccount, PbaStatus pbaStatus, String statusMessage) {
        paymentAccount.setPbaStatus(pbaStatus);
        paymentAccount.setStatusMessage(statusMessage);
    }

    public void addSuperUserToOrganisation(
            UserCreationRequest userCreationRequest,
            Organisation organisation) {

        if (userCreationRequest.getEmail() == null) {
            throw new InvalidRequest("Email cannot be null");
        }
        ProfessionalUser newProfessionalUser = new ProfessionalUser(
                RefDataUtil.removeEmptySpaces(userCreationRequest.getFirstName()),
                RefDataUtil.removeEmptySpaces(userCreationRequest.getLastName()),
                RefDataUtil.removeAllSpaces(userCreationRequest.getEmail().toLowerCase()),
                organisation);


        ProfessionalUser persistedSuperUser = professionalUserRepository.save(newProfessionalUser);

        List<UserAttribute> attributes
                = userAttributeService.addUserAttributesToSuperUser(persistedSuperUser,
                newProfessionalUser.getUserAttributes());
        newProfessionalUser.setUserAttributes(attributes);

        userAccountMapService.persistedUserAccountMap(persistedSuperUser, organisation.getPaymentAccounts());

        organisation.addProfessionalUser(persistedSuperUser.toSuperUser());

    }

    public void addContactInformationToOrganisation(
            List<ContactInformationCreationRequest> contactInformationCreationRequest,
            Organisation organisation) {

        if (contactInformationCreationRequest != null) {
            contactInformationCreationRequest.forEach(contactInfo -> {
                ContactInformation newContactInformation = new ContactInformation();
                newContactInformation = setNewContactInformationFromRequest(newContactInformation, contactInfo,
                        organisation);

                ContactInformation contactInformation = contactInformationRepository.save(newContactInformation);

                addDxAddressToContactInformation(contactInfo.getDxAddress(), contactInformation);

            });
        }
    }

    public ContactInformation setNewContactInformationFromRequest(ContactInformation contactInformation,
                                                                  ContactInformationCreationRequest contactInfo,
                                                                  Organisation organisation) {
        contactInformation.setAddressLine1(RefDataUtil.removeEmptySpaces(contactInfo.getAddressLine1()));
        contactInformation.setAddressLine2(RefDataUtil.removeEmptySpaces(contactInfo.getAddressLine2()));
        contactInformation.setAddressLine3(RefDataUtil.removeEmptySpaces(contactInfo.getAddressLine3()));
        contactInformation.setTownCity(RefDataUtil.removeEmptySpaces(contactInfo.getTownCity()));
        contactInformation.setCounty(RefDataUtil.removeEmptySpaces(contactInfo.getCounty()));
        contactInformation.setCountry(RefDataUtil.removeEmptySpaces(contactInfo.getCountry()));
        contactInformation.setPostCode(RefDataUtil.removeEmptySpaces(contactInfo.getPostCode()));
        contactInformation.setOrganisation(organisation);
        return contactInformation;
    }

    private void addDxAddressToContactInformation(List<DxAddressCreationRequest> dxAddressCreationRequest,
                                                  ContactInformation contactInformation) {
        if (dxAddressCreationRequest != null) {
            List<DxAddress> dxAddresses = new ArrayList<>();
            dxAddressCreationRequest.forEach(dxAdd -> {
                DxAddress dxAddress = new DxAddress(
                        RefDataUtil.removeEmptySpaces(dxAdd.getDxNumber()),
                        RefDataUtil.removeEmptySpaces(dxAdd.getDxExchange()),
                        contactInformation);
                dxAddresses.add(dxAddress);
            });
            dxAddressRepository.saveAll(dxAddresses);
        }
    }

    public List<Organisation> retrieveActiveOrganisationDetails() {

        List<Organisation> updatedOrganisationDetails = new ArrayList<>();
        Map<String, Organisation> activeOrganisationDtls = new ConcurrentHashMap<>();

        List<Organisation> activeOrganisations = getOrganisationByStatus(ACTIVE);

        activeOrganisations.forEach(organisation -> {
            if (!organisation.getUsers().isEmpty() && null != organisation.getUsers().get(ZERO_INDEX)
                    .getUserIdentifier()) {
                activeOrganisationDtls.put(organisation.getUsers().get(ZERO_INDEX).getUserIdentifier(), organisation);
            }
        });

        if (!CollectionUtils.isEmpty(activeOrganisations)) {

            RetrieveUserProfilesRequest retrieveUserProfilesRequest
                    = new RetrieveUserProfilesRequest(activeOrganisationDtls.keySet().stream().sorted()
                    .collect(Collectors.toList()));
            updatedOrganisationDetails = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,
                    retrieveUserProfilesRequest,
                    "false", activeOrganisationDtls);

        }
        return updatedOrganisationDetails;
    }

    @Override
    public OrganisationsDetailResponse retrieveAllOrganisations() {
        List<Organisation> retrievedOrganisations = organisationRepository.findAll();

        if (retrievedOrganisations.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }

        List<Organisation> pendingOrganisations = new ArrayList<>();
        List<Organisation> activeOrganisations = new ArrayList<>();
        List<Organisation> resultingOrganisations = new ArrayList<>();

        Map<String, Organisation> activeOrganisationDetails = new ConcurrentHashMap<>();

        retrievedOrganisations.forEach(organisation -> {
            if (organisation.isOrganisationStatusActive()) {
                activeOrganisations.add(organisation);
                if (!organisation.getUsers().isEmpty() && null != organisation.getUsers().get(ZERO_INDEX)
                        .getUserIdentifier()) {
                    activeOrganisationDetails.put(organisation.getUsers().get(ZERO_INDEX).getUserIdentifier(),
                            organisation);
                }
            } else if (organisation.getStatus() == OrganisationStatus.PENDING) {
                pendingOrganisations.add(organisation);
            }
        });

        List<Organisation> updatedActiveOrganisations = new ArrayList<>();

        if (!CollectionUtils.isEmpty(activeOrganisations)) {

            RetrieveUserProfilesRequest retrieveUserProfilesRequest
                    = new RetrieveUserProfilesRequest(activeOrganisationDetails.keySet().stream().sorted()
                    .collect(Collectors.toList()));
            updatedActiveOrganisations = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,
                    retrieveUserProfilesRequest,
                    "false", activeOrganisationDetails);
        }

        resultingOrganisations.addAll(pendingOrganisations);
        resultingOrganisations.addAll(updatedActiveOrganisations);

        return new OrganisationsDetailResponse(resultingOrganisations, true);
    }

    @Override
    public OrganisationResponse updateOrganisation(
            OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier) {

        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        //Into update Organisation service
        organisation.setName(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getName()));
        organisation.setStatus(OrganisationStatus.valueOf(organisationCreationRequest.getStatus()));
        organisation.setSraId(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getSraId()));
        organisation.setCompanyNumber(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getCompanyNumber()));
        organisation.setSraRegulated(Boolean.parseBoolean(RefDataUtil.removeEmptySpaces(organisationCreationRequest
                .getSraRegulated().toLowerCase())));
        organisation.setCompanyUrl(RefDataUtil.removeAllSpaces(organisationCreationRequest.getCompanyUrl()));
        Organisation savedOrganisation = organisationRepository.save(organisation);
        //Update Organisation service done

        if (isNotEmpty(savedOrganisation.getPaymentAccounts())) {
            updatePaymentAccounts(savedOrganisation.getPaymentAccounts());
        }

        return new OrganisationResponse(organisation);
    }

    public void updatePaymentAccounts(List<PaymentAccount> pbas) {
        //update Organisation's PBAs to ACCEPTED
        pbas.forEach(pba -> updateStatusAndMessage(pba, ACCEPTED, PBA_STATUS_MESSAGE_AUTO_ACCEPTED));
        paymentAccountRepository.saveAll(pbas);
    }

    @Override
    public Organisation getOrganisationByOrgIdentifier(String organisationIdentifier) {
        RefDataUtil.removeAllSpaces(organisationIdentifier);
        return organisationRepository.findByOrganisationIdentifier(organisationIdentifier);
    }

    @Override
    public OrganisationEntityResponse retrieveOrganisation(String organisationIdentifier) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);
        if (organisation == null) {
            throw new EmptyResultDataAccessException(ONE);

        } else if (ACTIVE.name().equalsIgnoreCase(organisation.getStatus().name())) {
            log.debug("{}:: Retrieving organisation", loggingComponentName);
            organisation.setUsers(RefDataUtil.getUserIdFromUserProfile(organisation.getUsers(), userProfileFeignClient,
                    false));
        }
        return new OrganisationEntityResponse(organisation, true);
    }

    @Override
    public OrganisationsDetailResponse findByOrganisationStatus(OrganisationStatus status) {

        List<Organisation> organisations = null;
        if (OrganisationStatus.PENDING.name().equalsIgnoreCase(status.name())) {

            organisations = getOrganisationByStatus(status);

        } else if (ACTIVE.name().equalsIgnoreCase(status.name())) {

            organisations = retrieveActiveOrganisationDetails();
        }

        if (CollectionUtils.isEmpty(organisations)) {
            throw new EmptyResultDataAccessException(ONE);

        }
        return new OrganisationsDetailResponse(organisations, true);
    }

    @Override
    @Transactional
    public DeleteOrganisationResponse deleteOrganisation(Organisation organisation, String prdAdminUserId) {
        DeleteOrganisationResponse deleteOrganisationResponse = new DeleteOrganisationResponse();
        switch (organisation.getStatus()) {
            case PENDING:
                return deleteOrganisationEntity(organisation, deleteOrganisationResponse, prdAdminUserId);
            case ACTIVE:
                deleteOrganisationResponse = deleteUserProfile(organisation, deleteOrganisationResponse);
                return deleteOrganisationResponse.getStatusCode() == ProfessionalApiConstants.STATUS_CODE_204
                        ? deleteOrganisationEntity(organisation, deleteOrganisationResponse, prdAdminUserId)
                        : deleteOrganisationResponse;
            default:
                throw new EmptyResultDataAccessException(ProfessionalApiConstants.ONE);
        }

    }

    private DeleteOrganisationResponse deleteOrganisationEntity(Organisation organisation,
                                                                DeleteOrganisationResponse deleteOrganisationResponse,
                                                                String prdAdminUserId) {
        organisationRepository.deleteById(organisation.getId());
        deleteOrganisationResponse.setStatusCode(ProfessionalApiConstants.STATUS_CODE_204);
        deleteOrganisationResponse.setMessage(ProfessionalApiConstants.DELETION_SUCCESS_MSG);
        log.info(loggingComponentName, organisation.getOrganisationIdentifier()
                + "::organisation deleted by::prdadmin::" + prdAdminUserId);
        return deleteOrganisationResponse;
    }

    private DeleteOrganisationResponse deleteUserProfile(Organisation organisation,
                                                         DeleteOrganisationResponse deleteOrganisationResponse) {

        // if user count more than one in the current organisation then throw exception
        if (ProfessionalApiConstants.USER_COUNT == professionalUserRepository
                .findByUserCountByOrganisationId(organisation.getId())) {
            ProfessionalUser user = organisation.getUsers()
                    .get(ProfessionalApiConstants.ZERO_INDEX).toProfessionalUser();
            NewUserResponse newUserResponse = RefDataUtil
                    .findUserProfileStatusByEmail(user.getEmailAddress(), userProfileFeignClient);

            if (ObjectUtils.isEmpty(newUserResponse.getIdamStatus())) {

                deleteOrganisationResponse.setStatusCode(ProfessionalApiConstants.ERROR_CODE_500);
                deleteOrganisationResponse.setMessage(ProfessionalApiConstants.ERR_MESG_500_ADMIN_NOTFOUNDUP);

            } else if (!IdamStatus.ACTIVE.name().equalsIgnoreCase(newUserResponse.getIdamStatus())) {
                // If user is not active in the up will send the request to delete
                Set<String> userIds = new HashSet<>();
                userIds.add(user.getUserIdentifier());
                DeleteUserProfilesRequest deleteUserRequest = new DeleteUserProfilesRequest(userIds);
                deleteOrganisationResponse = RefDataUtil
                        .deleteUserProfilesFromUp(deleteUserRequest, userProfileFeignClient);
            } else {
                deleteOrganisationResponse.setStatusCode(ProfessionalApiConstants.ERROR_CODE_400);
                deleteOrganisationResponse.setMessage(ProfessionalApiConstants.ERROR_MESSAGE_400_ADMIN_NOT_PENDING);
            }
        } else {
            deleteOrganisationResponse.setStatusCode(ProfessionalApiConstants.ERROR_CODE_400);
            deleteOrganisationResponse.setMessage(ProfessionalApiConstants.ERROR_MESSAGE_400_ORG_MORE_THAN_ONE_USER);
        }
        return deleteOrganisationResponse;
    }

    public List<Organisation> getOrganisationByStatus(OrganisationStatus status) {
        return organisationRepository.findByStatus(status);
    }

    @Override
    public ResponseEntity<Object> addPaymentAccountsToOrganisation(PbaAddRequest pbaAddRequest,
                                                      String organisationIdentifier, String userId) {
        Optional<Organisation> organisation = Optional.ofNullable(
                getOrganisationByOrgIdentifier(organisationIdentifier));

        if (organisation.isEmpty()) {
            log.error("{}:: {}", loggingComponentName, NO_ORG_FOUND_FOR_GIVEN_ID);
            throw new ResourceNotFoundException(NO_ORG_FOUND_FOR_GIVEN_ID);
        }

        validateOrganisationIsActive(organisation.get());
        professionalUserService.checkUserStatusIsActiveByUserId(userId);

        pbaAddRequest.getPaymentAccounts().removeIf(item -> item == null || "".equals(item.trim()));
        Pair<Set<String>, Set<String>> unsuccessfulPbas = getUnsuccessfulPbas(pbaAddRequest);

        if (!isEmpty(pbaAddRequest.getPaymentAccounts())) {
            addPbaAccountToOrganisation(pbaAddRequest.getPaymentAccounts(), organisation.get(), false, false);
        }
        return getResponse(unsuccessfulPbas.getLeft(),
                unsuccessfulPbas.getRight(), pbaAddRequest.getPaymentAccounts());

    }

    private Pair<Set<String>, Set<String>> getUnsuccessfulPbas(PbaAddRequest pbaAddRequest) {
        Set<String> invalidPaymentAccounts = null;
        String invalidPbas = PaymentAccountValidator.checkPbaNumberIsValid(pbaAddRequest.getPaymentAccounts(),
                Boolean.FALSE);
        if (StringUtils.isNotEmpty(invalidPbas)) {
            invalidPaymentAccounts = new HashSet<>(Arrays.asList(invalidPbas.split(",")));
            pbaAddRequest.getPaymentAccounts().removeAll(invalidPaymentAccounts);
        }

        Set<String> duplicatePaymentAccounts = paymentAccountValidator.getDuplicatePbas(
                pbaAddRequest.getPaymentAccounts());
        pbaAddRequest.getPaymentAccounts().removeAll(duplicatePaymentAccounts);

        return Pair.of(invalidPaymentAccounts, duplicatePaymentAccounts);
    }

    private ResponseEntity<Object> getResponse(Set<String> invalidPaymentAccounts,
                                               Set<String> duplicatePaymentAccounts, Set<String> validPaymentAccounts) {
        AddPbaResponse addPbaResponse = null;
        HttpStatus status = HttpStatus.CREATED;

        if ((!isEmpty(invalidPaymentAccounts) || !isEmpty(duplicatePaymentAccounts))
                && (!isEmpty(validPaymentAccounts))) {
            addPbaResponse = getAddPbaResponse(invalidPaymentAccounts,
                    duplicatePaymentAccounts, ERROR_MSG_PARTIAL_SUCCESS);
        } else if (validPaymentAccounts.isEmpty()) {
            status = HttpStatus.BAD_REQUEST;
            addPbaResponse = getAddPbaResponse(invalidPaymentAccounts, duplicatePaymentAccounts, null);
        }
        return ResponseEntity
                .status(status)
                .body(addPbaResponse);
    }

    private AddPbaResponse getAddPbaResponse(Set<String> invalidPaymentAccounts,
                                             Set<String> duplicatePaymentAccounts, String msg) {
        AddPbaResponse addPbaResponse;
        addPbaResponse = new AddPbaResponse();
        addPbaResponse.setMessage(msg);
        addPbaResponse.setReason(new FailedPbaReason(duplicatePaymentAccounts, invalidPaymentAccounts));
        return addPbaResponse;
    }

    public void validateOrganisationIsActive(Organisation existingOrganisation) {
        if (OrganisationStatus.ACTIVE != existingOrganisation.getStatus()) {
            log.error("{}:: {}", loggingComponentName, ORG_NOT_ACTIVE_NO_USERS_RETURNED);
            throw new EmptyResultDataAccessException(1);
        }
    }

}

