@F-001
Feature: F-001 : Get users by Organisation ID

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-001
  Scenario: must Get users by Organisation ID for active organisation
    Given a user with [active PRD admin role],
    And a successful call [to create an organisation] as in [S-001_Create_Organisation],
    And a successful call [to update an organisation] as in [S-001_Update_Organisation],
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Reference data API],
    And it is submitted to call the [Reference data Get users by org id API] operation of [Reference data API],
    Then a positive response is received,
    And the response has all other details as expected.





