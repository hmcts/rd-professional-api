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
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
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
    UserAccountMapRepository userAccountMapRepository;
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
            UserAccountMapRepository userAccountMapRepository,
            UserProfileFeignClient userProfileFeignClient,
            PrdEnumService prdEnumService,
            UserAttributeService userAttributeService
    ) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.paymentAccountRepository = paymentAccountRepository;
        this.contactInformationRepository = contactInformationRepository;
        this.dxAddressRepository = dxAddressRepository;
        this.userAccountMapRepository = userAccountMapRepository;
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

    private void addPbaAccountToOrganisation(
            List<String> paymentAccounts,
            Organisation organisation) {

        if (paymentAccounts != null) {
            paymentAccounts.forEach(pbaAccount -> {
                if (pbaAccount == null || !pbaAccount.matches("(PBA|pba).*") || !pbaAccount.matches("^[a-zA-Z0-9]+$")) {
                    throw new InvalidRequest("PBA number must start with PBA/pba and be followed by 7 alphanumeric characters");
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

        persistedUserAccountMap(persistedSuperUser,organisation.getPaymentAccounts());

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

    private void persistedUserAccountMap(ProfessionalUser persistedSuperUser, List<PaymentAccount> paymentAccounts) {

        if (!paymentAccounts.isEmpty()) {
            List<UserAccountMap> userAccountMaps = new ArrayList<>();
            log.debug("PaymentAccount is not empty");
            paymentAccounts.forEach(paymentAccount -> {
                userAccountMaps.add(new UserAccountMap(new UserAccountMapId(persistedSuperUser, paymentAccount)));
            });
            if (!CollectionUtils.isEmpty(userAccountMaps)) {
                userAccountMapRepository.saveAll(userAccountMaps);
            }
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

        log.info("Retrieving all organisations...");
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

    @Override
    public OrganisationResponse updateOrganisation(
            OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier) {

        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        log.info("Into update Organisation service");
        organisation.setName(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getName()));
        organisation.setStatus(OrganisationStatus.valueOf(organisationCreationRequest.getStatus()));
        organisation.setSraId(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getSraId()));
        organisation.setCompanyNumber(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getCompanyNumber()));
        organisation.setSraRegulated(Boolean.parseBoolean(RefDataUtil.removeEmptySpaces(organisationCreationRequest.getSraRegulated().toLowerCase())));
        organisation.setCompanyUrl(RefDataUtil.removeAllSpaces(organisationCreationRequest.getCompanyUrl()));
        organisationRepository.save(organisation);
        log.info("Update Organisation service done...");

        return new OrganisationResponse(organisation);
    }

    @Override
    public Organisation  getOrganisationByOrgIdentifier(String organisationIdentifier) {
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

            log.info("for ACTIVE::Status:");

            organisations = retrieveActiveOrganisationDetails();
        }

        if (CollectionUtils.isEmpty(organisations)) {

            throw new EmptyResultDataAccessException(1);

        }
        return new OrganisationsDetailResponse(organisations, true);
    }


}

