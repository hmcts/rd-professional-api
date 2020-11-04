@F-001
Feature: F-001 : Get all users and their details from organisation

   Background:
     Given an appropriate test context as detailed in the test data source
     And a successful call [to create an organisation] as in [S-001_Create_Organisation]
     And a successful call [to update an organisation] as in [S-001_Update_Organisation]
     And a successful call [to create PUM IDAM user] as in [S-001_Invite_A_User_In_IDAM]
     And a successful call [to invite an user] as in [S-001_Invite_User]

  @S-001
  Scenario: Get users and their details with roles from organisation
    And a user with [active PRD admin role]
    When a request is prepared with appropriate values
    And the request [is to be made on behalf of Reference data API]
    And it is submitted to call the [Reference data Get users with roles by org id API] operation of [Reference data API]
    Then a positive response is received.

  @S-002
  Scenario: Get users and their details without roles from organisation
    And a user with [active PRD admin role]
    When a request is prepared with appropriate values
    And the request [is to be made on behalf of Reference data API]
    And it is submitted to call the [Reference data Get users without roles by org id API] operation of [Reference data APIs]
    Then a positive response is received.

  @S-003
  Scenario: Get users and their details with invalid roleRequired param from organisation
    And a user with [active PRD admin role]
    When a request is prepared with appropriate values
    And the request [is to be made on behalf of Reference data API]
    And it is submitted to call the [Reference data Get users with invalid roleRequired param by org id API] operation of [Reference data APIs]
    Then a negative response is received.

  @S-011
  Scenario: Get users and their details with roles by PUM (external user) from organisation
    And a user with [active PUM user]
    When a request is prepared with appropriate values
    And the request [is to be made on behalf of Reference data API]
    And it is submitted to call the [Reference data Get users of an organisation by PUM (external user) by org id API] operation of [Reference data API]
    Then a positive response is received.

  @S-012
  Scenario: Get users and their details without roles by PUM (external user) from organisation
    And a user with [active PUM user]
    When a request is prepared with appropriate values
    And the request [is to be made on behalf of Reference data API]
    And it is submitted to call the [Reference data Get users of an organisation without roles by PUM (external user) by org id API] operation of [Reference data API]
    Then a positive response is received.

  @S-013
  Scenario: Get users and their details with invalid roleRequired param by PUM (external user) from organisation
    And a user with [active PUM user]
    When a request is prepared with appropriate values
    And the request [is to be made on behalf of Reference data API]
    And it is submitted to call the [Reference data Get users of an organisation wwith invalid roleRequired param by PUM (external user) by org id API] operation of [Reference data API]
    Then a negative response is received.