package uk.gov.hmcts.reform.professionalapi.helper;

import static uk.gov.hmcts.reform.professionalapi.domain.MFAStatus.EMAIL;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.PBA_STATUS_MESSAGE;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

public class BaseRepository {

    @Autowired
    public ContactInformationRepository contactInformationRepository;
    @Autowired
    public OrganisationRepository organisationRepository;
    @Autowired
    public DxAddressRepository dxAddressRepository;
    @Autowired
    public PaymentAccountRepository paymentAccountRepository;
    @Autowired
    public PrdEnumRepository prdEnumRepository;
    @Autowired
    public ProfessionalUserRepository professionalUserRepository;
    @Autowired
    public UserAccountMapRepository userAccountMapRepository;
    @Autowired
    public UserAttributeRepository userAttributeRepository;
    @Autowired
    public OrganisationMfaStatusRepository organisationMfaStatusRepository;

    public ContactInformation contactInformation;
    public DxAddress dxAddress;
    public Organisation organisation;
    public PaymentAccount paymentAccount;
    public ProfessionalUser professionalUser;
    public UserAccountMapId userAccountMapId;
    public UserAccountMap userAccountMap;
    public UserAttribute userAttribute;
    public PrdEnum prdEnum;
    public OrganisationMfaStatus organisationMfaStatus;

    @Before
    public void setUp() {
        organisation = new Organisation("some-name", OrganisationStatus.ACTIVE, "sra-id",
                "companyN", Boolean.FALSE, "company-url");
        organisation = organisationRepository.save(organisation);
        professionalUser = new ProfessionalUser("fName", "lName", "user@test.com",
                organisation);
        professionalUser = professionalUserRepository.save(professionalUser);
        organisationRepository.save(organisation);

        contactInformation = new ContactInformation();
        contactInformation.setAddressLine1(RefDataUtil.removeEmptySpaces("some-address1"));
        contactInformation.setAddressLine2(RefDataUtil.removeEmptySpaces("some-address2"));
        contactInformation.setAddressLine3(RefDataUtil.removeEmptySpaces("some-address3"));
        contactInformation.setTownCity(RefDataUtil.removeEmptySpaces("some-address1"));
        contactInformation.setCounty(RefDataUtil.removeEmptySpaces("some-county"));
        contactInformation.setCountry(RefDataUtil.removeEmptySpaces("some-country"));
        contactInformation.setPostCode(RefDataUtil.removeEmptySpaces("some-post-code"));
        contactInformation.setOrganisation(organisation);
        contactInformationRepository.save(contactInformation);

        dxAddress = new DxAddress("dx-number", "dx-exchange", contactInformation);
        dxAddress = dxAddressRepository.save(dxAddress);

        paymentAccount = new PaymentAccount("pba1234567");
        paymentAccount.setOrganisation(organisation);
        paymentAccount.setPbaStatus(ACCEPTED.name());
        paymentAccount.setStatusMessage(PBA_STATUS_MESSAGE);
        paymentAccount = paymentAccountRepository.save(paymentAccount);

        userAccountMapId = new UserAccountMapId(professionalUser, paymentAccount);
        userAccountMap = new UserAccountMap(userAccountMapId);
        userAccountMapRepository.save(userAccountMap);

        prdEnum = prdEnumRepository.findAll().get(0);

        userAttribute = new UserAttribute(professionalUser, prdEnum);
        userAttributeRepository.save(userAttribute);

        organisationMfaStatus = new OrganisationMfaStatus();
        organisationMfaStatus.setMfaStatus(EMAIL);
        organisationMfaStatus.setOrganisation(organisation);
        organisationMfaStatusRepository.save(organisationMfaStatus);
    }

    @After
    public void cleanupTestData() {
        dxAddressRepository.deleteAll();
        contactInformationRepository.deleteAll();
        userAttributeRepository.deleteAll();
        userAccountMapRepository.deleteAll();
        professionalUserRepository.deleteAll();
        paymentAccountRepository.deleteAll();
        organisationRepository.deleteAll();
        prdEnumRepository.deleteAll();
        organisationMfaStatusRepository.deleteAll();
    }
}