package uk.gov.hmcts.reform.professionalapi.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;

import java.util.UUID;

@Repository
public interface PrdEnumRepository extends JpaRepository<PrdEnum, UUID> {


}
