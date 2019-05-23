package uk.gov.hmcts.reform.professionalapi.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;

public interface UserAccountMapRepository extends JpaRepository<UserAccountMap,UUID> {
}
