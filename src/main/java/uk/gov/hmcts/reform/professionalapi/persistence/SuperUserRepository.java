package uk.gov.hmcts.reform.professionalapi.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

import java.util.List;
import java.util.UUID;

@Repository
public interface SuperUserRepository extends JpaRepository<SuperUser, UUID> {

}
