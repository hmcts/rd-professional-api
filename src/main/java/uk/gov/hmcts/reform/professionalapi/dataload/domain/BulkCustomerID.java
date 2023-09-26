package uk.gov.hmcts.reform.professionalapi.dataload.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity(name = "bulk_customer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCustomerID {

    @Column(name = "organisation_id")
    @Size(max = 64)
    private String organisationId;

    @Column(name = "bulk_customer_Id")
    @Size(max = 64)
    @Id
    private String bulkCustomerId;


    @Column(name = "sidam_id")
    @Size(max = 64)
    private String sidamId;

    @Column(name = "pba")
    @Size(max = 64)
    private String pba;

}
