package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ContactInformationRepositoryTest extends BaseRepository {

    @Test
    public void findAllTest() {
        List<ContactInformation> contactInformationList = contactInformationRepository.findAll();

        assertThat(contactInformationList).hasSize(1);
        assertThat(contactInformationList.get(0)).isEqualTo(contactInformation);
        assertThat(contactInformationList.get(0).getAddressLine1()).isEqualTo(contactInformation.getAddressLine1());
    }

    @Test
    public void findByIdTest() {
        Optional<ContactInformation> contactInfo = contactInformationRepository.findById(contactInformation.getId());

        assertThat(contactInfo.get()).isEqualTo(contactInformation);
        assertThat(contactInfo.get().getAddressLine1()).isEqualTo(contactInformation.getAddressLine1());
    }
}
