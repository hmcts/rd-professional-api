package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "dataload_schedular_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrdDataloadSchedulerJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "job_start_time")
    private LocalDateTime jobStartTime;

    @Column(name = "job_end_time")
    private LocalDateTime jobEndTime;

    @Column(name = "publishing_status")
    private String publishingStatus;
}


