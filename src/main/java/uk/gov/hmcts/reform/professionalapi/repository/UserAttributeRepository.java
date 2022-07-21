package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;

import java.util.UUID;

@Repository
public interface UserAttributeRepository extends JpaRepository<UserAttribute, UUID> {

}
