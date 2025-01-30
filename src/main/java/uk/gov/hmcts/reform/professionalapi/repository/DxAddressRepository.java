package uk.gov.hmcts.reform.professionalapi.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

import java.util.List;
import java.util.UUID;

@Repository
public interface DxAddressRepository extends JpaRepository<DxAddress, UUID> {

    @Query(value = "SELECT * FROM  dbrefdata.dx_address dx WHERE dx.contact_information_id = :contactInformationId", nativeQuery = true)
    List<DxAddress>  findByContactInformationId(@Param("contactInformationId")UUID contactInformationId);

}
