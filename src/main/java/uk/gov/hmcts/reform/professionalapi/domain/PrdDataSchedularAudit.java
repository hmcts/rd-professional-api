package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "dataloadSchedularAudit")
@Table(name = "dataload_schedular_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "prd_audit_scheduler_id_sequence",
    sequenceName = "prd_audit_scheduler_id_sequence", allocationSize = 1)
public class PrdDataSchedularAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prd_audit_scheduler_id_sequence")
    @Column(name = "id")
    private int id;

    @Column(name = "scheduler_name")
    private String schedulerName;

    @Column(name = "scheduler_start_time")
    private LocalDateTime schedulerStartTime;

    @Column(name = "scheduler_end_time")
    private LocalDateTime schedulerEndTime;

    @Column(name = "status")
    private String status;

    @Column(name = "api_name")
    private String apiName;

}
