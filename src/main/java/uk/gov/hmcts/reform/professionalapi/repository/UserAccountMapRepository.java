package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccountMapRepository extends JpaRepository<UserAccountMap,UUID> {

    Optional<UserAccountMap> findByUserAccountMapId(UserAccountMapId userAccountMapId);

    @Query(value = "Select * from dbrefdata.user_account_map ua where ua.professional_user_id=:profUserId",
        nativeQuery = true)
    List<UserAccountMap>  fetchByProfessionalUserId(UUID profUserId);
}
