package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountMapRepository extends JpaRepository<UserAccountMap,UUID> {

    Optional<UserAccountMap> findByUserAccountMapId(UserAccountMapId userAccountMapId);
}
