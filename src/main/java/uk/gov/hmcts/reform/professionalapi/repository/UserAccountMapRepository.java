package uk.gov.hmcts.reform.professionalapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;

@Repository
public interface UserAccountMapRepository extends JpaRepository<UserAccountMap,UUID> {

    Optional<UserAccountMap> findByUserAccountMapId(UserAccountMapId userAccountMapId);
}
