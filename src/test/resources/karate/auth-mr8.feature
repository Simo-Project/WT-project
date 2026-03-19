Feature: MR-8 authentication and role-based access control

  Scenario: Admin login returns JWT and admin endpoint is accessible
    Given url baseUrl
    And path 'api', 'auth', 'login'
    And request
      """
      {
        "username": "admin",
        "password": "admin123"
      }
      """
    When method post
    Then status 200
    And match response.token == '#string'
    And match response.role == 'ADMIN'

    * def adminToken = response.token

    Given url baseUrl
    And path 'api', 'admin', 'requests'
    And header Authorization = 'Bearer ' + adminToken
    When method get
    Then status 200
    And match response == '#[]'

  Scenario: Unauthenticated request to admin endpoint gets 401
    Given url baseUrl
    And path 'api', 'admin', 'requests'
    When method get
    Then status 401

  Scenario: Resident cannot access admin endpoint
    Given url baseUrl
    And path 'api', 'auth', 'login'
    And request
      """
      {
        "username": "resident",
        "password": "resident123"
      }
      """
    When method post
    Then status 200
    And match response.token == '#string'
    And match response.role == 'RESIDENT'

    * def residentToken = response.token

    Given url baseUrl
    And path 'api', 'admin', 'requests'
    And header Authorization = 'Bearer ' + residentToken
    When method get
    Then status 403