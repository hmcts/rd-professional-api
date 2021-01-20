package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;

import java.util.Arrays;

@Component
@Primary
public class PactJwtGrantedAuthoritiesConverter extends JwtGrantedAuthoritiesConverter {

    public PactJwtGrantedAuthoritiesConverter(IdamRepository idamRepository) {
        super(idamRepository);
    }

    @Override
    public UserInfo getUserInfo() {

        return UserInfo.builder().roles(Arrays.asList("pui-finance-manager")).uid("someUid").build();
    }
}
