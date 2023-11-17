package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import static javax.persistence.GenerationType.AUTO;

@Entity(name = "org_attributes")
@NoArgsConstructor
@Getter
@Setter
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
