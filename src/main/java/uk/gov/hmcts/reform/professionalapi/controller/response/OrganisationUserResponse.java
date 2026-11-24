package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class OrganisationUserResponse {
    private String userIdentifier;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime lastUpdated;
    private LocalDateTime deleted;
    private List<UserAccessType> userAccessTypes = new ArrayList<>();

    public OrganisationUserResponse(ProfessionalUser user) {
        this.userIdentifier = user.getUserIdentifier();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
        this.lastUpdated = user.getLastUpdated();
        this.deleted = user.getDeleted();
        this.userAccessTypes =
                user.getUserConfiguredAccesses().stream().map(RefDataUtil::fromUserConfiguredAccess).toList();
    }
}
