package uk.gov.hmcts.reform.professionalapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
class DxAddressRepositoryTest extends BaseRepository {

    @Test
    void test_findAll() {
        List<DxAddress> dxAddresses = dxAddressRepository.findAll();

        assertThat(dxAddresses).hasSize(1);
        assertThat(dxAddresses.get(0)).isEqualTo(dxAddress);
        assertThat(dxAddresses.get(0).getId()).isEqualTo(dxAddress.getId());
    }

    @Test
    void test_findById() {
        Optional<DxAddress> dxAdd = dxAddressRepository.findById(dxAddress.getId());

        assertThat(dxAdd).contains(dxAddress);
        assertThat(dxAdd.get().getId()).isEqualTo(dxAddress.getId());
    }
}
