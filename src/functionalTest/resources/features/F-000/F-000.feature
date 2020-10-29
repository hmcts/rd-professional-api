@F-000
Feature: F-000 : Ref data health check

  Background:
    Given an appropriate test context as detailed in the test data source

  @S-000
  Scenario: must see Ref data health is UP
    When a request is prepared with appropriate values,
    And the request [is to be made on behalf of Reference data API],
    And it is submitted to call the [Reference data health API] operation of [Reference data API],
    Then a positive response is received,
    And the response has all other details as expected.

