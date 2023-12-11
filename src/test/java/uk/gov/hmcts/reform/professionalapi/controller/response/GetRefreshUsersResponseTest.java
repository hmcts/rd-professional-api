package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class GetRefreshUsersResponseTest {

    @Test
    void test_should_hold_values_after_creation() {
        GetRefreshUsersResponse getRefreshUsersResponse = new GetRefreshUsersResponse();
        getRefreshUsersResponse.setUsers(new HashSet<>());
        getRefreshUsersResponse.setMoreAvailable(false);

        assertThat(getRefreshUsersResponse.getUsers()).isNotNull();
        assertThat(getRefreshUsersResponse.isMoreAvailable()).isFalse();
    }
}
