package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.SingletonOrgType;

import java.util.Optional;

@Repository
public interface SingletonOrgTypeRepository  extends JpaRepository<SingletonOrgType, String> {

    Optional<SingletonOrgType> findByOrgType(String orgType);
}
