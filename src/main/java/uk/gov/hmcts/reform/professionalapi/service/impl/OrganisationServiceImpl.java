package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.tuple.Pair;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DeleteUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsWithPbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.AddPbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.FailedPbaReason;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LOG_ERROR_BODY_START;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.NO_ORG_FOUND_FOR_GIVEN_ID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ONE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NOT_ACTIVE_NO_USERS_RETURNED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE_ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE_AUTO_ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ZERO_INDEX;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationStatusValidatorImpl.doesListContainActiveStatus;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationStatusValidatorImpl.getOrgStatusEnumsExcludingActiveStatus;
import static uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationStatusValidatorImpl.validateAndReturnStatusList;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

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

    @Override
    @Transactional
    public void deletePaymentsOfOrganisation(Set<String> paymentAccounts, Organisation organisation) {
        final Set<String> paymentAccountsUpper = paymentAccounts.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        paymentAccountRepository.deleteByPbaNumberUpperCase(paymentAccountsUpper);
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

        if (!StringUtils.isBlank(contactInfo.getUprn())) {
            contactInformation.setUprn(RefDataUtil.removeEmptySpaces(contactInfo.getUprn()));
        }
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

        return new OrganisationsDetailResponse(resultingOrganisations, true, false, false);
    }

    @Override
    public OrganisationResponse updateOrganisation(
            OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier) {

        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        //Into update Organisation service
        organisation.setName(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getName()));
        organisation.setStatus(OrganisationStatus.valueOf(organisationCreationRequest.getStatus()));
        organisation.setStatusMessage(organisationCreationRequest.getStatusMessage());
        organisation.setSraId(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getSraId()));
        organisation.setCompanyNumber(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getCompanyNumber()));
        organisation.setSraRegulated(Boolean.parseBoolean(RefDataUtil.removeEmptySpaces(organisationCreationRequest
                .getSraRegulated().toLowerCase())));
        organisation.setCompanyUrl(RefDataUtil.removeAllSpaces(organisationCreationRequest.getCompanyUrl()));
        Organisation savedOrganisation = organisationRepository.save(organisation);
        //Update Organisation service done

        if (isNotEmpty(savedOrganisation.getPaymentAccounts())
                && organisationCreationRequest.getStatus().equals("ACTIVE")) {
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
    public OrganisationEntityResponse retrieveOrganisation(
            String organisationIdentifier, boolean isPendingPbaRequired) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);
        if (organisation == null) {
            throw new EmptyResultDataAccessException(ONE);

        } else if (ACTIVE.name().equalsIgnoreCase(organisation.getStatus().name())) {
            log.debug("{}:: Retrieving organisation", loggingComponentName);
            organisation.setUsers(RefDataUtil.getUserIdFromUserProfile(organisation.getUsers(), userProfileFeignClient,
                    false));
        }
        if (!organisation.getContactInformation().isEmpty()
                && organisation.getContactInformation().size() > 1) {
            sortContactInfoByCreatedDateAsc(organisation);
        }

        return new OrganisationEntityResponse(organisation, true, isPendingPbaRequired, false);
    }

    @Override
    public OrganisationsDetailResponse findByOrganisationStatus(String status) {
        List<Organisation> organisations = new ArrayList<>();

        List<String> statuses = new ArrayList<>(validateAndReturnStatusList(status));

        if (doesListContainActiveStatus(statuses)) {
            organisations = retrieveActiveOrganisationDetails();
        }

        List<OrganisationStatus> enumStatuses = getOrgStatusEnumsExcludingActiveStatus(statuses);

        organisations.addAll(getOrganisationByStatuses(enumStatuses));

        if (CollectionUtils.isEmpty(organisations)) {
            throw new EmptyResultDataAccessException(ONE);
        }

        return new OrganisationsDetailResponse(organisations, true, false, false);
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

    public List<Organisation> getOrganisationByStatuses(List<OrganisationStatus> enumStatuses) {
        return organisationRepository.findByStatusIn(enumStatuses);
    }

    public List<Organisation> getOrganisationByStatus(OrganisationStatus status) {
        return organisationRepository.findByStatus(status);
    }

    public ResponseEntity<Object> getOrganisationsByPbaStatus(String pbaStatus) {

        if (Stream.of(PbaStatus.values()).noneMatch(status -> status.name().equalsIgnoreCase(pbaStatus))) {
            throw new InvalidRequest("Invalid PBA status provided");
        }

        List<Organisation> organisations = organisationRepository.findByPbaStatus(PbaStatus.valueOf(pbaStatus));

        LinkedHashMap<String, List<Organisation>> orgPbaMap = organisations
                .stream()
                .collect(Collectors.groupingBy(
                        Organisation::getOrganisationIdentifier, LinkedHashMap::new, Collectors.toList()));

        var organisationsWithPbaStatusResponses = new ArrayList<OrganisationsWithPbaStatusResponse>();

        orgPbaMap.forEach((k, v) -> organisationsWithPbaStatusResponses.add(
                new OrganisationsWithPbaStatusResponse(k, v.get(0).getStatus(),
                        v.get(0).getPaymentAccounts()
                                .stream()
                                .filter(paymentAccount ->
                                        paymentAccount.getPbaStatus().equals(PbaStatus.valueOf(pbaStatus)))
                                .collect(Collectors.toList()))));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(organisationsWithPbaStatusResponses);
    }

    @Override
    public ResponseEntity<Object> addPaymentAccountsToOrganisation(PbaRequest pbaRequest,
                                                                   String organisationIdentifier, String userId) {
        Optional<Organisation> organisation = Optional.ofNullable(
                getOrganisationByOrgIdentifier(organisationIdentifier));

        if (organisation.isEmpty()) {
            log.error(LOG_ERROR_BODY_START, loggingComponentName, NO_ORG_FOUND_FOR_GIVEN_ID);
            throw new ResourceNotFoundException(NO_ORG_FOUND_FOR_GIVEN_ID);
        }

        validateOrganisationIsActive(organisation.get());
        professionalUserService.checkUserStatusIsActiveByUserId(userId);

        Pair<Set<String>, Set<String>> unsuccessfulPbas = getUnsuccessfulPbas(pbaRequest);

        if (!isEmpty(pbaRequest.getPaymentAccounts())) {
            addPbaAccountToOrganisation(
                    pbaRequest.getPaymentAccounts(), organisation.get(), false, false);
        }
        return getResponse(unsuccessfulPbas.getLeft(),
                unsuccessfulPbas.getRight(), pbaRequest.getPaymentAccounts());

    }

    @Override
    @Transactional
    public void addContactInformationsToOrganisation(
            List<ContactInformationCreationRequest> contactInformationCreationRequests,
            String organisationIdentifier) {

        Optional<Organisation> organisationOptional = Optional.ofNullable(
                getOrganisationByOrgIdentifier(organisationIdentifier));


        if (organisationOptional.isEmpty()) {
            log.error(LOG_ERROR_BODY_START, loggingComponentName, NO_ORG_FOUND_FOR_GIVEN_ID);
            throw new ResourceNotFoundException(NO_ORG_FOUND_FOR_GIVEN_ID);
        }

        Organisation organisation = organisationOptional.get();

        addContactInformationToOrganisation(contactInformationCreationRequests, organisation);


    }



    private Pair<Set<String>, Set<String>> getUnsuccessfulPbas(PbaRequest pbaRequest) {
        Set<String> invalidPaymentAccounts = null;
        paymentAccountValidator.isPbaRequestEmptyOrNull(pbaRequest);
        pbaRequest.getPaymentAccounts().removeIf(item -> item == null || "".equals(item.trim()));

        String invalidPbas = PaymentAccountValidator.checkPbaNumberIsValid(pbaRequest.getPaymentAccounts(),
                Boolean.FALSE);
        if (StringUtils.isNotEmpty(invalidPbas)) {
            invalidPaymentAccounts = new HashSet<>(Arrays.asList(invalidPbas.split(",")));
            pbaRequest.getPaymentAccounts().removeAll(invalidPaymentAccounts);
        }

        Set<String> duplicatePaymentAccounts = paymentAccountValidator.getDuplicatePbas(
                pbaRequest.getPaymentAccounts());
        pbaRequest.getPaymentAccounts().removeAll(duplicatePaymentAccounts);

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
            log.error(LOG_ERROR_BODY_START, loggingComponentName, ORG_NOT_ACTIVE_NO_USERS_RETURNED);
            throw new EmptyResultDataAccessException(1);
        }
    }

    private void sortContactInfoByCreatedDateAsc(Organisation organisation) {
        List<ContactInformation> sortedContactInfoByCreatedDate = organisation.getContactInformation()
                .stream()
                .sorted(Comparator.comparing(ContactInformation::getCreated))
                .collect(Collectors.toList());

        organisation.setContactInformations(sortedContactInfoByCreatedDate);
    }

    @Override
    @Transactional
    public void deleteMultipleAddressOfGivenOrganisation(Set<UUID> idsSet) {
        contactInformationRepository.deleteByIdIn(idsSet);
    }

}

