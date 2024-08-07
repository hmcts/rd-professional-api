package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

import java.util.UUID;

@Repository
public interface DxAddressRepository extends JpaRepository<DxAddress, UUID> {

    @Modifying
    @Query(value = "delete from dbrefdata.dx_address da "
        + "where da.dx_number=:dxNumber and da.contact_information_id =:id",
        nativeQuery = true)
    void deleteByContactInfoId(String dxNumber, UUID id);

}
