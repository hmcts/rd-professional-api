package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAttributeRepository extends JpaRepository<UserAttribute, UUID> {

    @Modifying
    @Query(value = "delete from dbrefdata.user_attribute ua where ua.professional_user_id=:profUserId",
        nativeQuery = true)
    void deleteByProfessionalUserId(UUID profUserId);

    @Query(value = "Select * from dbrefdata.user_attribute ua " +
        "where ua.professional_user_id=:profUserId and prd_enum_type='ADMIN_ROLE'",
        nativeQuery = true)
    List<UserAttribute> fetchByProfessionalUserIdAndPrdEnumType(UUID profUserId);

}
