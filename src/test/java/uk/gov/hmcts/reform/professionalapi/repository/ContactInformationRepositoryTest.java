package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class ContactInformationRepositoryTest {

    @Autowired
    ContactInformationRepository contactInformationRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    ContactInformation contactInformation = new ContactInformation();
    Organisation organisation = new Organisation("some-name", OrganisationStatus.ACTIVE,
            "sra-id", "companyN", Boolean.FALSE, "company-url");

    @Before
    public void setUp() {
        organisationRepository.save(organisation);

        contactInformation.setAddressLine1(RefDataUtil.removeEmptySpaces("some-address1"));
        contactInformation.setAddressLine2(RefDataUtil.removeEmptySpaces("some-address2"));
        contactInformation.setAddressLine3(RefDataUtil.removeEmptySpaces("some-address3"));
        contactInformation.setTownCity(RefDataUtil.removeEmptySpaces("some-address1"));
        contactInformation.setCounty(RefDataUtil.removeEmptySpaces("some-county"));
        contactInformation.setCountry(RefDataUtil.removeEmptySpaces("some-country"));
        contactInformation.setPostCode(RefDataUtil.removeEmptySpaces("some-post-code"));
        contactInformation.setOrganisation(organisation);

        contactInformationRepository.save(contactInformation);
    }

    @Test
    public void findAllTest() {
        List<ContactInformation> contactInformationList = contactInformationRepository.findAll();

        assertThat(contactInformationList).hasSize(1);
        assertThat(contactInformationList.get(0)).isEqualTo(contactInformation);
    }

    @Test
    public void findByIdTest() {
        Optional<ContactInformation> contactInfo = contactInformationRepository.findById(contactInformation.getId());

        assertThat(contactInfo.get()).isEqualTo(contactInformation);
    }
}

