package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DxAddressRepositoryTest extends BaseRepository {

    @Test
    public void test_findAll() {
        List<DxAddress> dxAddresses = dxAddressRepository.findAll();

        assertThat(dxAddresses).hasSize(1);
        assertThat(dxAddresses.get(0)).isEqualTo(dxAddress);
        assertThat(dxAddresses.get(0).getId()).isEqualTo(dxAddress.getId());
    }

    @Test
    public void test_findById() {
        Optional<DxAddress> dxAdd = dxAddressRepository.findById(dxAddress.getId());

        assertThat(dxAdd.get()).isEqualTo(dxAddress);
        assertThat(dxAdd.get().getId()).isEqualTo(dxAddress.getId());
    }
}
