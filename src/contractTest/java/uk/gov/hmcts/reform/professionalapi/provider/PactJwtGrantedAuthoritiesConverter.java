package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.randomUUID;

@Component
@Primary
public class PactJwtGrantedAuthoritiesConverter extends JwtGrantedAuthoritiesConverter {

    public PactJwtGrantedAuthoritiesConverter(IdamRepository idamRepository) {
        super(idamRepository);
    }


    public UserInfo getUserInfo() {
        return UserInfo.builder().roles(asList("pui-finance-manager")).uid(randomUUID()).build();
    }
}
