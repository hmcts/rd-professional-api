package uk.gov.hmcts.reform.professionalapi.service.impl;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Setter;
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
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;
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

    public void addPbaAccountToOrganisation(Set<String> paymentAccounts, Organisation organisation) {
        if (paymentAccounts != null) {
            PaymentAccountValidator.checkPbaNumberIsValid(paymentAccounts);

            paymentAccounts.forEach(pbaAccount -> {
                PaymentAccount paymentAccount = new PaymentAccount(pbaAccount.toUpperCase());
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

        List<String> jurisdictionIds = userCreationRequest.getJurisdictions().stream().map(Jurisdiction::getId).collect(Collectors.toList());

        ProfessionalUser persistedSuperUser = professionalUserRepository.save(newProfessionalUser);

        List<UserAttribute> attributes = userAttributeService.addUserAttributesToSuperUserWithJurisdictions(persistedSuperUser, newProfessionalUser.getUserAttributes(), jurisdictionIds);
        newProfessionalUser.setUserAttributes(attributes);

        userAccountMapService.persistedUserAccountMap(persistedSuperUser, organisation.getPaymentAccounts());

        organisation.addProfessionalUser(persistedSuperUser.toSuperUser());

    }

    private void addContactInformationToOrganisation(
            List<ContactInformationCreationRequest> contactInformationCreationRequest,
            Organisation organisation) {

        if (contactInformationCreationRequest != null) {
            contactInformationCreationRequest.forEach(contactInfo -> {
                ContactInformation newContactInformation = new ContactInformation();
                newContactInformation.setAddressLine1(RefDataUtil.removeEmptySpaces(contactInfo.getAddressLine1()));
                newContactInformation.setAddressLine2(RefDataUtil.removeEmptySpaces(contactInfo.getAddressLine2()));
                newContactInformation.setAddressLine3(RefDataUtil.removeEmptySpaces(contactInfo.getAddressLine3()));
                newContactInformation.setTownCity(RefDataUtil.removeEmptySpaces(contactInfo.getTownCity()));
                newContactInformation.setCounty(RefDataUtil.removeEmptySpaces(contactInfo.getCounty()));
                newContactInformation.setCountry(RefDataUtil.removeEmptySpaces(contactInfo.getCountry()));
                newContactInformation.setPostCode(RefDataUtil.removeEmptySpaces(contactInfo.getPostCode()));
                newContactInformation.setOrganisation(organisation);

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

    public List<Organisation> retrieveActiveOrganisationDetails() {

        List<Organisation> updatedOrganisationDetails = new ArrayList<>();
        Map<String, Organisation> activeOrganisationDtls = new ConcurrentHashMap<>();

        List<Organisation> activeOrganisations = organisationRepository.findByStatus(OrganisationStatus.ACTIVE);

        activeOrganisations.forEach(organisation -> {
            if (!organisation.getUsers().isEmpty() && null != organisation.getUsers().get(0).getUserIdentifier()) {
                activeOrganisationDtls.put(organisation.getUsers().get(0).getUserIdentifier(), organisation);
            }
        });

        if (!CollectionUtils.isEmpty(activeOrganisations)) {

            RetrieveUserProfilesRequest retrieveUserProfilesRequest = new RetrieveUserProfilesRequest(activeOrganisationDtls.keySet().stream().sorted().collect(Collectors.toList()));
            updatedOrganisationDetails = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient, retrieveUserProfilesRequest,
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
                if (!organisation.getUsers().isEmpty() && null != organisation.getUsers().get(0).getUserIdentifier()) {
                    activeOrganisationDetails.put(organisation.getUsers().get(0).getUserIdentifier(), organisation);
                }
            } else if (organisation.getStatus() == OrganisationStatus.PENDING) {
                pendingOrganisations.add(organisation);
            }
        });

        List<Organisation> updatedActiveOrganisations = new ArrayList<>();

        if (!CollectionUtils.isEmpty(activeOrganisations)) {

            RetrieveUserProfilesRequest retrieveUserProfilesRequest
                    = new RetrieveUserProfilesRequest(activeOrganisationDetails.keySet().stream().sorted().collect(Collectors.toList()));
            updatedActiveOrganisations = RefDataUtil.getMultipleUserProfilesFromUp(userProfileFeignClient, retrieveUserProfilesRequest,
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
            organisation.setUsers(RefDataUtil.getUserIdFromUserProfile(organisation.getUsers(), userProfileFeignClient, false));
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


}

