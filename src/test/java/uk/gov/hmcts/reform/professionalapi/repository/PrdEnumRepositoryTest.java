package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnumId;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class PrdEnumRepositoryTest {

    @Autowired
    PrdEnumRepository prdEnumRepository;

    PrdEnumId prdEnumId = new PrdEnumId();
    PrdEnum prdEnum = new PrdEnum(prdEnumId, "enum-name", "enum-desc");

    @Before
    public void setUp() {
        prdEnumRepository.save(prdEnum);
    }

    @Test
    public void test_findAll() {
        List<PrdEnum> prdEnums = prdEnumRepository.findAll();

        assertThat(prdEnums).hasSize(1);
        assertThat(prdEnums.get(0)).isEqualTo(prdEnum);
    }
}
