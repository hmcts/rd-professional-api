package uk.gov.hmcts.reform.professionalapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;

@Entity(name = "user_account_map")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
public class UserAccountMap implements Serializable {

    @EmbeddedId
    private UserAccountMapId  userAccountMapId;

    @Column(name = "DEFAULTED")
    private static final Boolean DEFAULTED = false;

    public UserAccountMap(UserAccountMapId  userAccountMapId) {

        this.userAccountMapId = userAccountMapId;
    }

}
