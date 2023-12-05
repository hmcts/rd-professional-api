package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.UserConfiguredAccess;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserConfiguredAccessRepository extends JpaRepository<UserConfiguredAccess, UUID> {

    List<UserConfiguredAccess> findByUserConfiguredAccessId_ProfessionalUser_Id(UUID professionalUserId);

    List<UserConfiguredAccess> findByUserConfiguredAccessId_ProfessionalUser_IdIn(List<UUID> professionalUserIds);
}
