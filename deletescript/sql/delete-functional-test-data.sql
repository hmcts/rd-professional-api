delete from user_attribute
where professional_user_id in
(
select id from professional_user where organisation_id in (
select id from organisation where company_number like '%com'
and company_url like '%company-url'
and sra_id like '%sra-id-number1'
)
);

delete from user_account_map
where professional_user_id in
(
 select up.id from organisation org , professional_user up
 where up.organisation_id in (select id from organisation where company_number like '%com'
and company_url like '%company-url'
and sra_id like '%sra-id-number1')
);

delete from payment_account
where organisation_id in
(
 select id from organisation where company_number like '%com'
and company_url like '%company-url'
and sra_id like '%sra-id-number1'
);

delete from dx_address
where contact_information_id in
(
 select contactinfo.id from organisation org , contact_information contactinfo, professional_user up
 where up.organisation_id in (select id from organisation where company_number like '%com'
and company_url like '%company-url'
and sra_id like '%sra-id-number1')
);

delete from contact_information
where organisation_id in
(
 select id from organisation where company_number like '%com'
and company_url like '%company-url'
and sra_id like '%sra-id-number1'
);

delete from organisation_mfa_status
where organisation_id in
(
select id from organisation where company_number like '%com'
and company_url like '%company-url'
and sra_id like '%sra-id-number1'

);

delete from professional_user
where organisation_id in
(
select id from organisation where company_number like '%com'
and company_url like '%company-url'
and sra_id like '%sra-id-number1'

);


delete from org_attributes
where organisation_id in
(
 select id from organisation where company_number like '%com'
and company_url like '%company-url'
and sra_id like '%sra-id-number1'
);


delete from organisation
where company_number like '%com'
and company_url like '%company-url'
and sra_id like '%sra-id-number1';

commit;