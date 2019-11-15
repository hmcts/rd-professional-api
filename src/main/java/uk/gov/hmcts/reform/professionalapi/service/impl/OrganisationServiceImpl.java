package uk.gov.hmcts.reform.professionalapi.service.impl;

import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGeneratorConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.RetrieveUserProfilesRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;


@Service
@Slf4j
public class OrganisationServiceImpl implements OrganisationService {
    OrganisationRepository organisationRepository;
    ProfessionalUserRepository professionalUserRepository;
    PaymentAccountRepository paymentAccountRepository;
    DxAddressRepository dxAddressRepository;
    ContactInformationRepository contactInformationRepository;
    PrdEnumRepository prdEnumRepository;
    UserAccountMapService userAccountMapService;
    UserProfileFeignClient userProfileFeignClient;
    PrdEnumService prdEnumService;
    UserAttributeService userAttributeService;

    @Autowired
    public OrganisationServiceImpl(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            PaymentAccountRepository paymentAccountRepository,
            DxAddressRepository dxAddressRepository,
            ContactInformationRepository contactInformationRepository,
            PrdEnumRepository prdEnumRepository,
            UserAccountMapService userAccountMapService,
            UserProfileFeignClient userProfileFeignClient,
            PrdEnumService prdEnumService,
            UserAttributeService userAttributeService
    ) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.paymentAccountRepository = paymentAccountRepository;
        this.contactInformationRepository = contactInformationRepository;
        this.dxAddressRepository = dxAddressRepository;
        this.userAccountMapService = userAccountMapService;
        this.prdEnumRepository = prdEnumRepository;
        this.userProfileFeignClient = userProfileFeignClient;
        this.prdEnumService = prdEnumService;
        this.userAttributeService = userAttributeService;
    }

    @Override
    @Transactional
    public OrganisationResponse createOrganisationFrom(
            OrganisationCreationRequest organisationCreationRequest) {

        Organisation newOrganisation = new Organisation(
                RefDataUtil.removeEmptySpaces(organisationCreationRequest.getName()),
                OrganisationStatus.PENDING,
                RefDataUtil.removeEmptySpaces(organisationCreationRequest.getSraId()),
                RefDataUtil.removeEmptySpaces(organisationCreationRequest.getCompanyNumber()),
                Boolean.parseBoolean(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getSraRegulated().toLowerCase())),
                RefDataUtil.removeAllSpaces(organisationCreationRequest.getCompanyUrl())
        );

        Organisation organisation = saveOrganisation(newOrganisation);

        addPbaAccountToOrganisation(organisationCreationRequest.getPaymentAccount(), organisation);

        addSuperUserToOrganisation(organisationCreationRequest.getSuperUser(), organisation);

        addContactInformationToOrganisation(organisationCreationRequest.getContactInformation(), organisation);

        return new OrganisationResponse(organisation);
    }

    private Organisation saveOrganisation(Organisation organisation) {
        Organisation persistedOrganisation = null;
        try {
            persistedOrganisation = organisationRepository.save(organisation);
        } catch (ConstraintViolationException ex) {
            organisation.setOrganisationIdentifier(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));
            persistedOrganisation = organisationRepository.save(organisation);
        }
        return persistedOrganisation;
    }

    public void addPbaAccountToOrganisation(List<String> paymentAccounts, Organisation organisation) {

        if (paymentAccounts != null) {
            paymentAccounts.forEach(pbaAccount -> {
                if (pbaAccount == null || !pbaAccount.matches("(PBA|pba).*") || !pbaAccount.matches("^[a-zA-Z0-9]+$")) {
                    throw new InvalidRequest("PBA number must start with PBA/pba and be followed by 7 alphanumeric characters, you entered: " + pbaAccount);
                }

                PaymentAccount paymentAccount = new PaymentAccount(pbaAccount);
                paymentAccount.setOrganisation(organisation);
                PaymentAccount persistedPaymentAccount = paymentAccountRepository.save(paymentAccount);
                organisation.addPaymentAccount(persistedPaymentAccount);
            });
        }
    }

    private void addSuperUserToOrganisation(
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

        List<String> jurisdictionIds = userCreationRequest.getJurisdictions().stream().map(jurisdiction -> jurisdiction.getId()).collect(Collectors.toList());

        ProfessionalUser persistedSuperUser = professionalUserRepository.save(newProfessionalUser);

        List<UserAttribute> attributes = userAttributeService.addUserAttributesToSuperUserWithJurisdictions(persistedSuperUser, newProfessionalUser.getUserAttributes(), jurisdictionIds);
        newProfessionalUser.setUserAttributes(attributes);

        userAccountMapService.persistedUserAccountMap(persistedSuperUser,organisation.getPaymentAccounts());

        organisation.addProfessionalUser(persistedSuperUser.toSuperUser());

    }

    private void addContactInformationToOrganisation(
            List<ContactInformationCreationRequest> contactInformationCreationRequest,
            Organisation organisation) {

        if (contactInformationCreationRequest != null) {
            contactInformationCreationRequest.forEach(contactInfo -> {
                ContactInformation newContactInformation = new ContactInformation(
                        RefDataUtil.removeEmptySpaces(contactInfo.getAddressLine1()),
                        RefDataUtil.removeEmptySpaces(contactInfo.getAddressLine2()),
                        RefDataUtil.removeEmptySpaces(contactInfo.getAddressLine3()),
                        RefDataUtil.removeEmptySpaces(contactInfo.getTownCity()),
                        RefDataUtil.removeEmptySpaces(contactInfo.getCounty()),
                        RefDataUtil.removeEmptySpaces(contactInfo.getCountry()),
                        RefDataUtil.removeEmptySpaces(contactInfo.getPostCode()),
                        organisation);

                ContactInformation contactInformation = contactInformationRepository.save(newContactInformation);

                addDxAddressToContactInformation(contactInfo.getDxAddress(), contactInformation);

            });
        }
    }

    private void addDxAddressToContactInformation(List<DxAddressCreationRequest> dxAddressCreationRequest, ContactInformation contactInformation) {
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

    @Override
    public OrganisationsDetailResponse retrieveOrganisations() {

        List<Organisation> pendingOrganisations = organisationRepository.findByStatus(OrganisationStatus.PENDING);

        List<Organisation> activeOrganisations = retrieveActiveOrganisationDetails();

        if (pendingOrganisations.isEmpty() && activeOrganisations.isEmpty()) {

            log.info("No Organisations Retrieved...");
            throw new EmptyResultDataAccessException(1);
        }

        pendingOrganisations.addAll(activeOrganisations);

        return new OrganisationsDetailResponse(pendingOrganisations, true);
    }

    public List<Organisation> retrieveActiveOrganisationDetails() {

        List<Organisation> updatedOrganisationDetails = new ArrayList<>();
        Map<String,Organisation> activeOrganisationDtls = new ConcurrentHashMap<String,Organisation>();

        List<Organisation> activeOrganisations = organisationRepository.findByStatus(OrganisationStatus.ACTIVE);

        activeOrganisations.forEach(organisation -> {
            if (organisation.getUsers().size() > 0 && null != organisation.getUsers().get(0).getUserIdentifier()) {
                activeOrganisationDtls.put(organisation.getUsers().get(0).getUserIdentifier(),organisation);
            }
        });

        if (!CollectionUtils.isEmpty(activeOrganisations)) {

            RetrieveUserProfilesRequest retrieveUserProfilesRequest = new RetrieveUserProfilesRequest(activeOrganisationDtls.keySet().stream().sorted().collect(Collectors.toList()));
            updatedOrganisationDetails = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,retrieveUserProfilesRequest,
                    "false", activeOrganisationDtls);

        }
        return updatedOrganisationDetails;
    }

    public ResponseEntity retrieveActiveOrganisationDetails(Pageable pageable) {

        List<Organisation> updatedOrganisationDetails = new ArrayList<>();
        Map<String,Organisation> activeOrganisationDtls = new ConcurrentHashMap<String,Organisation>();
        OrganisationsDetailResponse organisationsDetailResponse = null;

        Page<Organisation> activeOrganisations = organisationRepository.findByStatus(
                OrganisationStatus.ACTIVE, pageable);
        List<Organisation> activeOrganisationsTemp = activeOrganisations.getContent();
        if (CollectionUtils.isEmpty(activeOrganisationsTemp)) {
            throw new EmptyResultDataAccessException(1);
        } else {
            activeOrganisationsTemp.forEach(organisation -> {
                if (organisation.getUsers().size() > 0 && null != organisation.getUsers().get(0).getUserIdentifier()) {
                    activeOrganisationDtls.put(organisation.getUsers().get(0).getUserIdentifier(),organisation);
                }
            });
        }

        if (!CollectionUtils.isEmpty(activeOrganisationDtls)) {
            RetrieveUserProfilesRequest retrieveUserProfilesRequest = new RetrieveUserProfilesRequest(activeOrganisationDtls.keySet().stream().sorted().collect(Collectors.toList()));
            updatedOrganisationDetails = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,retrieveUserProfilesRequest,
                    "false", activeOrganisationDtls);
            organisationsDetailResponse = new OrganisationsDetailResponse(
                    updatedOrganisationDetails, true);
        }

        HttpHeaders headers = RefDataUtil.generateResponseEntityWithPaginationHeader(pageable, activeOrganisations, null);
        return ResponseEntity.status(200).headers(headers).body(organisationsDetailResponse);
    }

    public List<Organisation> retrieveActiveOrganisationDetails(List<Organisation> organisations) {

        List<Organisation> updatedOrganisationDetails = new ArrayList<>();
        Map<String,Organisation> activeOrganisationDtls = new ConcurrentHashMap<String,Organisation>();
        List<Organisation> pendingOrganisations = new ArrayList<>();
        List<Organisation> activeOrganisations = new ArrayList<>();

        if (!organisations.isEmpty()) {
            organisations.forEach(organisation -> {
                if (organisation.getStatus().isActive() && organisation.getUsers().size() > 0 && organisation.getUsers().get(0).getUserIdentifier() != null) {
                    activeOrganisationDtls.put(organisation.getUsers().get(0).getUserIdentifier(),organisation);
                    activeOrganisations.add(organisation);
                } else {
                    if (organisation.getStatus().isPending()) {
                        pendingOrganisations.add(organisation);
                    }
                }
            });
            //log.info("Retrieving all organisations..." + activeOrganisations.toString());
            if (activeOrganisationDtls.size() > 0) {
                RetrieveUserProfilesRequest retrieveUserProfilesRequest = new RetrieveUserProfilesRequest(activeOrganisationDtls.keySet().stream().collect(Collectors.toList()));
                updatedOrganisationDetails = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient,retrieveUserProfilesRequest,
                        "false", activeOrganisationDtls);
                if (updatedOrganisationDetails != null) {
                    pendingOrganisations.addAll(updatedOrganisationDetails);
                }
            }
        }

        return pendingOrganisations;
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
        organisation.setSraRegulated(Boolean.parseBoolean(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getSraRegulated().toLowerCase())));
        organisation.setCompanyUrl(RefDataUtil.removeAllSpaces(organisationCreationRequest.getCompanyUrl()));
        organisationRepository.save(organisation);
        //Update Organisation service done

        return new OrganisationResponse(organisation);
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
            throw new EmptyResultDataAccessException(1);

        } else if (OrganisationStatus.ACTIVE.name().equalsIgnoreCase(organisation.getStatus().name())) {
            log.debug("Retrieving organisation");
            organisation.setUsers(RefDataUtil.getUserIdFromUserProfile(organisation.getUsers(),userProfileFeignClient, false));
        }
        return new OrganisationEntityResponse(organisation, true);
    }

    @Override
    public OrganisationsDetailResponse findByOrganisationStatus(OrganisationStatus status) {

        List<Organisation> organisations = null;

        if (OrganisationStatus.PENDING.name().equalsIgnoreCase(status.name())) {

            organisations = organisationRepository.findByStatus(status);

        } else if (OrganisationStatus.ACTIVE.name().equalsIgnoreCase(status.name())) {

            organisations = retrieveActiveOrganisationDetails();
        }

        if (CollectionUtils.isEmpty(organisations)) {

            throw new EmptyResultDataAccessException(1);

        }
        return new OrganisationsDetailResponse(organisations, true);
    }


    @Override
    public ResponseEntity retrieveOrganisationsWithPageable(Pageable pageable) {
        List<OrganisationStatus> statusList = new ArrayList<>();
        statusList.add(OrganisationStatus.PENDING);
        statusList.add(OrganisationStatus.ACTIVE);
        Page<Organisation> organisationsPage = organisationRepository.findByStatusIn(
                statusList, pageable);

        OrganisationsDetailResponse organisationsDetailResponse =
                new OrganisationsDetailResponse(retrieveActiveOrganisationDetails(organisationsPage.getContent()),
                        true);

        if (organisationsDetailResponse.getOrganisations().isEmpty()) {
            log.info("No Organisations Retrieved...");
            throw new EmptyResultDataAccessException(1);
        } else {
            log.info("Retrieving all organisations..." + organisationsDetailResponse.getOrganisations().size());
            HttpHeaders headers = RefDataUtil.generateResponseEntityWithPaginationHeader(pageable, organisationsPage, null);
            return ResponseEntity.status(200).headers(headers).body(organisationsDetailResponse);
        }

    }

    @Override
    public ResponseEntity findByOrganisationStatusWithPageable(OrganisationStatus status, Pageable pageable) {
        Page<Organisation> organisations = null;
        ResponseEntity responseEntity = null;
        if (OrganisationStatus.PENDING.name().equalsIgnoreCase(status.name())) {
            organisations = organisationRepository.findByStatus(status, pageable);
            if (CollectionUtils.isEmpty(organisations.getContent())) {
                throw new EmptyResultDataAccessException(1);
            } else {
                OrganisationsDetailResponse organisationsDetailResponse = new OrganisationsDetailResponse(
                        organisations.getContent(), true);
                HttpHeaders headers = RefDataUtil.generateResponseEntityWithPaginationHeader(pageable, organisations, null);
                responseEntity = ResponseEntity.status(200).headers(headers).body(organisationsDetailResponse);
            }
        } else if (OrganisationStatus.ACTIVE.name().equalsIgnoreCase(status.name())) {
            log.info("for ACTIVE::Status:");
            responseEntity = retrieveActiveOrganisationDetails(pageable);
        }

        return responseEntity;
    }
}

