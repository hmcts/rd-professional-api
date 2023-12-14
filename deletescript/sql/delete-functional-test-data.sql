delete
from
    user_attribute u
where
        u.professional_user_id in
        (
            select
                pu.id
            from
                professional_user pu
                    join organisation o on
                        pu.organisation_id = o.id
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    user_account_map uam
where
        uam.professional_user_id in
        (
            select
                pu.id
            from
                professional_user pu
                    join organisation o on
                        pu.organisation_id = o.id
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    payment_account pa
where
        pa.organisation_id in
        (
            select
                o.id
            from
                organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    dx_address dx
where
        dx.contact_information_id in
        (
            select
                c.id
            from
                organisation o
                    join contact_information c on
                        o.id = c.organisation_id
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    contact_information c
where
        c.organisation_id in
        (
            select
                o.id
            from
                organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    organisation_mfa_status mfa
where
        mfa.organisation_id in
        (
            select
                o.id
            from
                organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'

        );

delete
from
    professional_user pu
where
        pu.organisation_id in
        (
            select
                o.id
            from
                organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'

        );


delete
from
    org_attributes oa
where
        oa.organisation_id in
        (
            select
                o.id
            from
                organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'

        );


delete
from
    organisation o
where
      o.company_url like '%-prd-func-test-company-url'
  and o.name like '%-prd-func-test-name'
  and o.sra_id like '%-prd-func-test-sra-id';

commit;