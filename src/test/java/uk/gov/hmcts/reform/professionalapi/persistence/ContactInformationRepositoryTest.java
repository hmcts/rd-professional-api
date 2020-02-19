package uk.gov.hmcts.reform.professionalapi.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ContactInformationRepositoryTest {

    @Autowired
    ContactInformationRepository contactInformationRepository;

    ContactInformation contactInformation = new ContactInformation();

    @Before
    public void setUp() {
        contactInformation.setAddressLine1(RefDataUtil.removeEmptySpaces("some-address1"));
        contactInformation.setAddressLine2(RefDataUtil.removeEmptySpaces("some-address2"));
        contactInformation.setAddressLine3(RefDataUtil.removeEmptySpaces("some-address3"));
        contactInformation.setTownCity(RefDataUtil.removeEmptySpaces("some-address1"));
        contactInformation.setCounty(RefDataUtil.removeEmptySpaces("some-county"));
        contactInformation.setCountry(RefDataUtil.removeEmptySpaces("some-country"));
        contactInformation.setPostCode(RefDataUtil.removeEmptySpaces("some-post-code"));
        contactInformation.setOrganisation(mock(Organisation.class));

        contactInformationRepository.save(contactInformation);
    }

    @Test
    public void findAllTest() {
        List<ContactInformation> contactInformation = contactInformationRepository.findAll();

        assertThat(contactInformation).hasSize(1);
        assertThat(contactInformation.get(0)).isEqualTo(contactInformation);
    }
}

