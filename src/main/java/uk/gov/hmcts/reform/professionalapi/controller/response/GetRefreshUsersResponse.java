package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.RefreshUser;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class GetRefreshUsersResponse {

    private Set<RefreshUser> users;
    private boolean moreAvailable;

    public GetRefreshUsersResponse(Set<RefreshUser> users, boolean moreAvailable) {
        this.users = users;
        this.moreAvailable = moreAvailable;
    }
}
