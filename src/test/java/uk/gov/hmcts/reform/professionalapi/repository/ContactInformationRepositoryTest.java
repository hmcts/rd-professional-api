package uk.gov.hmcts.reform.professionalapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
class ContactInformationRepositoryTest extends BaseRepository {

    @Test
    void test_findAll() {
        List<ContactInformation> contactInformationList = contactInformationRepository.findAll();

        assertThat(contactInformationList).hasSize(1);
        assertThat(contactInformationList.get(0)).isEqualTo(contactInformation);
        assertThat(contactInformationList.get(0).getAddressLine1()).isEqualTo(contactInformation.getAddressLine1());
        assertThat(contactInformationList.get(0).getUprn()).isEqualTo(contactInformation.getUprn());
    }

    @Test
    void test_findById() {
        Optional<ContactInformation> contactInfo = contactInformationRepository.findById(contactInformation.getId());

        assertThat(contactInfo).contains(contactInformation);
        assertThat(contactInfo.get().getAddressLine1()).isEqualTo(contactInformation.getAddressLine1());
        assertThat(contactInfo.get().getUprn()).isEqualTo(contactInformation.getUprn());

    }

    @Test
    void should_add_contactInformations_Test() {
        List<ContactInformation> contactInformations = addContactInformations();

        assertThat(contactInformations.get(0).getUprn()).isEqualTo("uprn-1");
        assertThat(contactInformations.get(1).getUprn()).isEqualTo("uprn-2");

    }

    private List<ContactInformation> addContactInformations() {


        contactInformation = new ContactInformation();
        contactInformation.setUprn("uprn-1");
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

        List<ContactInformation> contactInformations = new ArrayList<>();

        contactInformations.add(contactInformation);

        contactInformation = new ContactInformation();
        contactInformation.setUprn("uprn-2");
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
        contactInformations.add(contactInformation);


        return contactInformationRepository.saveAll(contactInformations);

    }
}

