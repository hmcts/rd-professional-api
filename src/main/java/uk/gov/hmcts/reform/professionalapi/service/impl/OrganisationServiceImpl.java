package uk.gov.hmcts.reform.professionalapi.service.impl;

import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

import java.util.List;
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
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
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
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;


@Service
@Slf4j
public class OrganisationServiceImpl implements OrganisationService {
    OrganisationRepository organisationRepository;
    ProfessionalUserRepository professionalUserRepository;
    PaymentAccountRepository paymentAccountRepository;
    DxAddressRepository dxAddressRepository;
    ContactInformationRepository contactInformationRepository;
    UserAttributeRepository userAttributeRepository;
    PrdEnumRepository prdEnumRepository;
    UserAccountMapRepository userAccountMapRepository;
    UserProfileFeignClient userProfileFeignClient;

    private static final String SIDAM_ROLE = "SIDAM_ROLE";
    private static final String ADMIN_ROLE = "ADMIN_ROLE";
    private static final String JURISD_ID = "JURISD_ID";

    @Autowired
    public OrganisationServiceImpl(
            OrganisationRepository organisationRepository,
            ProfessionalUserRepository professionalUserRepository,
            PaymentAccountRepository paymentAccountRepository,
            DxAddressRepository dxAddressRepository,
            ContactInformationRepository contactInformationRepository,
            UserAttributeRepository userAttributeRepository,
            PrdEnumRepository prdEnumRepository,
            UserAccountMapRepository userAccountMapRepository,
            UserProfileFeignClient userProfileFeignClient
    ) {

        this.organisationRepository = organisationRepository;
        this.professionalUserRepository = professionalUserRepository;
        this.paymentAccountRepository = paymentAccountRepository;
        this.contactInformationRepository = contactInformationRepository;
        this.dxAddressRepository = dxAddressRepository;
        this.userAccountMapRepository = userAccountMapRepository;
        this.userAttributeRepository = userAttributeRepository;
        this.prdEnumRepository = prdEnumRepository;
        this.userProfileFeignClient = userProfileFeignClient;
    }

    @Override
    @Transactional
    public OrganisationResponse createOrganisationFrom(
            OrganisationCreationRequest organisationCreationRequest) {

        Organisation newOrganisation = new Organisation(
                PbaAccountUtil.removeEmptySpaces(organisationCreationRequest.getName()),
                OrganisationStatus.PENDING,
                PbaAccountUtil.removeEmptySpaces(organisationCreationRequest.getSraId()),
                PbaAccountUtil.removeEmptySpaces(organisationCreationRequest.getCompanyNumber()),
                organisationCreationRequest.getSraRegulated(),
                PbaAccountUtil.removeAllSpaces(organisationCreationRequest.getCompanyUrl())
        );

        Organisation organisation = saveOrganisation(newOrganisation);

        addPbaAccountToOrganisation(organisationCreationRequest.getPaymentAccount(), organisation);

        addSuperUserToOrganisation(organisationCreationRequest.getSuperUser(), organisation);

        addContactInformationToOrganisation(organisationCreationRequest.getContactInformation(), organisation);

        organisationRepository.save(organisation);

        return new OrganisationResponse(organisation);
    }

    private List<UserAttribute> addAllAttributes(List<UserAttribute> attributes, ProfessionalUser user, List<String> jurisdictionIds) {
        prdEnumRepository.findAll().stream().forEach(prdEnum -> {
            String enumType = prdEnum.getPrdEnumId().getEnumType();
            if (enumType.equalsIgnoreCase(SIDAM_ROLE)
                    || enumType.equalsIgnoreCase(ADMIN_ROLE)
                    || (enumType.equalsIgnoreCase(JURISD_ID) && jurisdictionIds.contains(prdEnum.getEnumName()))) {
                PrdEnum newPrdEnum = new PrdEnum(prdEnum.getPrdEnumId(), prdEnum.getEnumName(), prdEnum.getEnumDescription());
                UserAttribute userAttribute = new UserAttribute(user, newPrdEnum);
                UserAttribute persistedAttribute = userAttributeRepository.save(userAttribute);
                attributes.add(persistedAttribute);
            }
        });
        return attributes;
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

        ProfessionalUser newProfessionalUser = new ProfessionalUser(
                PbaAccountUtil.removeEmptySpaces(userCreationRequest.getFirstName()),
                PbaAccountUtil.removeEmptySpaces(userCreationRequest.getLastName()),
                PbaAccountUtil.removeAllSpaces(userCreationRequest.getEmail()),
                organisation);

        List<String> jurisdictionIds = userCreationRequest.getJurisdictions().stream().map(jurisdiction -> jurisdiction.get("id")).collect(Collectors.toList());

        ProfessionalUser persistedSuperUser = professionalUserRepository.save(newProfessionalUser);

        List<UserAttribute> attributes = addAllAttributes(newProfessionalUser.getUserAttributes(), persistedSuperUser, jurisdictionIds);
        newProfessionalUser.setUserAttributes(attributes);

        persistedUserAccountMap(persistedSuperUser,organisation.getPaymentAccounts());

        organisation.addProfessionalUser(persistedSuperUser);

    }

    private void addContactInformationToOrganisation(
            List<ContactInformationCreationRequest> contactInformationCreationRequest,
            Organisation organisation) {

        if (contactInformationCreationRequest != null) {
            contactInformationCreationRequest.forEach(contactInfo -> {
                ContactInformation newContactInformation = new ContactInformation(
                        PbaAccountUtil.removeEmptySpaces(contactInfo.getAddressLine1()),
                        PbaAccountUtil.removeEmptySpaces(contactInfo.getAddressLine2()),
                        PbaAccountUtil.removeEmptySpaces(contactInfo.getAddressLine3()),
                        PbaAccountUtil.removeEmptySpaces(contactInfo.getTownCity()),
                        PbaAccountUtil.removeEmptySpaces(contactInfo.getCounty()),
                        PbaAccountUtil.removeEmptySpaces(contactInfo.getCountry()),
                        PbaAccountUtil.removeEmptySpaces(contactInfo.getPostCode()),
                        organisation);

                ContactInformation contactInformation = contactInformationRepository.save(newContactInformation);

                addDxAddressToContactInformation(contactInfo.getDxAddress(), contactInformation);

                contactInformationRepository.save(contactInformation);
                organisation.addContactInformation(contactInformation);
            });
        }
    }

    private void addDxAddressToContactInformation(List<DxAddressCreationRequest> dxAddressCreationRequest, ContactInformation contactInformation) {
        if (dxAddressCreationRequest != null) {
            dxAddressCreationRequest.forEach(dxAdd -> {
                DxAddress dxAddress = new DxAddress(
                        PbaAccountUtil.removeEmptySpaces(dxAdd.getDxNumber()),
                        PbaAccountUtil.removeEmptySpaces(dxAdd.getDxExchange()),
                        contactInformation);
                dxAddress = dxAddressRepository.save(dxAddress);
                contactInformation.addDxAddress(dxAddress);
            });
        }
    }

    private void persistedUserAccountMap(ProfessionalUser persistedSuperUser, List<PaymentAccount> paymentAccounts) {

        if (!paymentAccounts.isEmpty()) {
            log.debug("PaymentAccount is not empty");
            paymentAccounts.forEach(paymentAccount -> {

                userAccountMapRepository.save(new UserAccountMap(new UserAccountMapId(persistedSuperUser, paymentAccount)));
            });
        }
    }

    @Override
    public OrganisationsDetailResponse retrieveOrganisations() {
        List<Organisation> organisations = organisationRepository.findAll();

        log.debug("Retrieving all organisations...");

        if (organisations.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }

        organisations = organisations.stream()
                .map(organisation -> {

                    if (OrganisationStatus.ACTIVE == organisation.getStatus()) {

                        organisation.setUsers(PbaAccountUtil.getUserIdFromUserProfile(organisation.getUsers(), userProfileFeignClient));
                        return organisation;
                    }
                    return organisation;
                }).collect(Collectors.toList());

        return new OrganisationsDetailResponse(organisations, true);
    }

    @Override
    public OrganisationResponse updateOrganisation(
            OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier) {

        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);

        log.info("Into update Organisation service");
        organisation.setName(PbaAccountUtil.removeEmptySpaces(organisationCreationRequest.getName()));
        organisation.setStatus(organisationCreationRequest.getStatus());
        organisation.setSraId(PbaAccountUtil.removeEmptySpaces(organisationCreationRequest.getSraId()));
        organisation.setCompanyNumber(PbaAccountUtil.removeEmptySpaces(organisationCreationRequest.getCompanyNumber()));
        organisation.setSraRegulated(organisationCreationRequest.getSraRegulated());
        organisation.setCompanyUrl(PbaAccountUtil.removeAllSpaces(organisationCreationRequest.getCompanyUrl()));
        organisationRepository.save(organisation);
        log.info("Update Organisation service done...");

        return new OrganisationResponse(organisation);
    }

    @Override
    public Organisation  getOrganisationByOrgIdentifier(String organisationIdentifier) {
        PbaAccountUtil.removeAllSpaces(organisationIdentifier);
        return organisationRepository.findByOrganisationIdentifier(organisationIdentifier);
    }

    @Override
    public OrganisationEntityResponse retrieveOrganisation(String organisationIdentifier) {
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);
        if (organisation == null) {
            throw new EmptyResultDataAccessException(1);

        } else if (OrganisationStatus.ACTIVE.name().equalsIgnoreCase(organisation.getStatus().name())) {
            log.debug("Retrieving organisation with ID " + organisationIdentifier);
            organisation.setUsers(PbaAccountUtil.getUserIdFromUserProfile(organisation.getUsers(),userProfileFeignClient));
        }
        return new OrganisationEntityResponse(organisation, true);
    }

    @Override
    public OrganisationsDetailResponse findByOrganisationStatus(OrganisationStatus status) {
        List<Organisation> organisations = organisationRepository.findByStatus(status);

        if (CollectionUtils.isEmpty(organisations)) {
            throw new EmptyResultDataAccessException(1);

        } else if (OrganisationStatus.ACTIVE.name().equalsIgnoreCase(status.name())) {

            log.info("for ACTIVE::Status:");
            organisations = organisations.stream()
                    .map(organisation -> {
                        organisation.setUsers(PbaAccountUtil.getUserIdFromUserProfile(organisation.getUsers(), userProfileFeignClient));
                        return organisation;
                    }).collect(Collectors.toList());
        }
        return new OrganisationsDetailResponse(organisations, true);
    }


}

