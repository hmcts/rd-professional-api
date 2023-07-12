package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import static javax.persistence.GenerationType.AUTO;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "org_attributes")
public class OrgAttribute {

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
