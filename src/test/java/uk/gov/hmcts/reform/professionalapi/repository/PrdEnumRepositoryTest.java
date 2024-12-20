package uk.gov.hmcts.reform.professionalapi.repository;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
class PrdEnumRepositoryTest extends BaseRepository {

    @Test
    void test_findAll() {
        List<PrdEnum> prdEnums = prdEnumRepository.findAll();

        assertThat(prdEnums).hasSize(49);
        assertThat(prdEnums.get(0).getPrdEnumId().getEnumCode()).isZero();
    }

    @Test
    void test_findAll_containsCaaRoles_and_unspec_ccd_roles() {
        List<PrdEnum> prdEnums = prdEnumRepository.findAll();

        assertThat(prdEnums.get(37).getEnumName()).isEqualTo("pui-caa");
        assertThat(prdEnums.get(38).getEnumName()).isEqualTo("caseworker-caa");
        assertThat(prdEnums.get(43).getEnumName()).isEqualTo("caseworker-civil");
        assertThat(prdEnums.get(44).getEnumName()).isEqualTo("caseworker-civil-solicitor");
    }

    @Test
    void test_findAll_manageOrg_roles() {
        List<PrdEnum> prdEnums = prdEnumRepository.findAll();

        assertThat(prdEnums.get(45).getEnumName()).isEqualTo("caseworker-employment");
        assertThat(prdEnums.get(46).getEnumName()).isEqualTo("caseworker-employment-legalrep-solicitor");
    }

    @Test
    void test_findAll_manageOrg_privateLaw_roles() {
        List<PrdEnum> prdEnums = prdEnumRepository.findAll();

        assertThat(prdEnums.get(47).getEnumName()).isEqualTo("caseworker-privatelaw");
        assertThat(prdEnums.get(48).getEnumName()).isEqualTo("caseworker-privatelaw-solicitor");
    }
}
