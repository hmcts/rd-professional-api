delete
from
    dbrefdata.user_attribute u
where
        u.professional_user_id in
        (
            select
                pu.id
            from
                dbrefdata.professional_user pu
                    join dbrefdata.organisation o on
                        pu.organisation_id = o.id
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    dbrefdata.user_account_map uam
where
        uam.professional_user_id in
        (
            select
                pu.id
            from
                dbrefdata.professional_user pu
                    join dbrefdata.organisation o on
                        pu.organisation_id = o.id
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    dbrefdata.payment_account pa
where
        pa.organisation_id in
        (
            select
                o.id
            from
                dbrefdata.organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    dbrefdata.dx_address dx
where
        dx.contact_information_id in
        (
            select
                c.id
            from
                dbrefdata.organisation o
                    join dbrefdata.contact_information c on
                        o.id = c.organisation_id
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    dbrefdata.contact_information c
where
        c.organisation_id in
        (
            select
                o.id
            from
                dbrefdata.organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'
        );

delete
from
    dbrefdata.organisation_mfa_status mfa
where
        mfa.organisation_id in
        (
            select
                o.id
            from
                dbrefdata.organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'

        );

delete
from
    dbrefdata.professional_user pu
where
        pu.organisation_id in
        (
            select
                o.id
            from
                dbrefdata.organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'

        );


delete
from
    dbrefdata.org_attributes oa
where
        oa.organisation_id in
        (
            select
                o.id
            from
                dbrefdata.organisation o
            where
                  o.company_url like '%-prd-func-test-company-url'
              and o.name like '%-prd-func-test-name'
              and o.sra_id like '%-prd-func-test-sra-id'

        );


delete
from
    dbrefdata.organisation o
where
      o.company_url like '%-prd-func-test-company-url'
  and o.name like '%-prd-func-test-name'
  and o.sra_id like '%-prd-func-test-sra-id';

commit;