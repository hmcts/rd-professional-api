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
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class DxAddressRepositoryTest {

    @Autowired
    DxAddressRepository dxAddressRepository;

    ContactInformation contactInformation = new ContactInformation();

    DxAddress dxAddress = new DxAddress("dx-number", "dx-exchange", contactInformation);

    @Before
    public void setUp() {
        dxAddressRepository.save(dxAddress);
    }

    @Test
    public void test_findAll() {
        List<DxAddress> dxAddresses = dxAddressRepository.findAll();

        assertThat(dxAddresses).hasSize(1);
        assertThat(dxAddresses.get(0)).isEqualTo(dxAddress);
    }

    @Test
    public void test_findById() {
        Optional<DxAddress> dxAdd = dxAddressRepository.findById(dxAddress.getId());

        assertThat(dxAdd.get()).isEqualTo(dxAddress);
    }

}
