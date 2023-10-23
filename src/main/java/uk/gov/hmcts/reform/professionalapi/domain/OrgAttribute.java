package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

import static jakarta.persistence.GenerationType.AUTO;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "org_attributes")
public class OrgAttribute implements Serializable {

    @Id
    @GeneratedValue(strategy = AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "ORGANISATION_ID", nullable = false)
    private Organisation organisation;

    @Column(name = "KEY")
    @Size(max = 256)
    private String key;

    @Column(name = "VALUE")
    @Size(max = 256)
    private String value;

}
