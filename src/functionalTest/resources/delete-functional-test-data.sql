delete from user_attribute
where professional_user_id in
(
select id from professional_user where organisation_id in (
select org.id from organisation org, professional_user up
where up.email_address like any (values('%@prdfunctestuser.com'))
and org.id=up.organisation_id)
);

delete from user_account_map
where professional_user_id in
(
 select up.id from organisation org , professional_user up
 where up.organisation_id = org.id
 and up.email_address like any (values('%@prdfunctestuser.com'))
);

delete from payment_account
where organisation_id in
(
 select org.id from organisation org , professional_user up
 where up.organisation_id = org.id
 and up.email_address like any (values('%@prdfunctestuser.com'))
);

delete from dx_address
where contact_information_id in
(
 select contactinfo.id from organisation org , contact_information contactinfo, professional_user up
 where up.organisation_id = org.id and contactinfo.organisation_id = org.id
 and up.email_address like any (values('%@prdfunctestuser.com'))
);

delete from contact_information
where organisation_id in
(
 select org.id from organisation org , professional_user up
 where up.organisation_id = org.id
 and up.email_address like any (values('%@prdfunctestuser.com'))
);

delete from professional_user
where organisation_id in
(
 select org.id from organisation org , professional_user up
 where up.organisation_id = org.id
 and up.email_address like any (values('%@prdfunctestuser.com'))
);

delete from organisation
where id not in
(
 select organisation_id from professional_user
);

commit;