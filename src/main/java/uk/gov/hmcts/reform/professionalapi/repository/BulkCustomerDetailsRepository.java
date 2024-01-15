package uk.gov.hmcts.reform.professionalapi.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.BulkCustomerDetails;

import java.util.UUID;

@Repository
public interface BulkCustomerDetailsRepository extends JpaRepository<BulkCustomerDetails, UUID> {

    @Query(value = "SELECT * FROM  dbrefdata.bulk_customer_details bcd WHERE bcd.bulk_customer_id = :bulkCustomerId "
            + "and bcd.sidam_id = :idamId", nativeQuery = true)
    BulkCustomerDetails findByBulkCustomerId(@Param("bulkCustomerId")String bulkCustomerId,
                                             @Param("idamId") String idamId);


    @Modifying
    @Query(value = "delete from dbrefdata.bulk_customer_details blk where blk.ORGANISATION_ID in (:val)",nativeQuery = true)
    void deleteByOrganistion(String val);

}
