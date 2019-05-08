package uk.gov.hmcts.reform.professionalapi.service.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.hmcts.reform.professionalapi.domain.DxAddress;

public interface DxAddressRepository extends JpaRepository<DxAddress, UUID> {

}
