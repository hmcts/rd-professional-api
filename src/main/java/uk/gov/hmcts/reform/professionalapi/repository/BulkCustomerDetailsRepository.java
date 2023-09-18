package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.BulkCustomerDetails;

import java.util.UUID;

@Repository
public interface BulkCustomerDetailsRepository extends JpaRepository<BulkCustomerDetails, UUID> {

    BulkCustomerDetails findByBulkCustomerId(String bulkCustomerId);

}
