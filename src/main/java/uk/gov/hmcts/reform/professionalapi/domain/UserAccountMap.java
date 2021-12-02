package uk.gov.hmcts.reform.professionalapi.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;

import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "user_account_map")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
public class UserAccountMap implements Serializable {

    @EmbeddedId
    private UserAccountMapId  userAccountMapId;

    @Column(name = "DEFAULTED")
    private final Boolean defaulted = false;

    public UserAccountMap(UserAccountMapId  userAccountMapId) {

        this.userAccountMapId = userAccountMapId;
    }

}
