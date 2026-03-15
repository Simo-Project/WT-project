Feature: Resident request workflow

  Scenario: Resident can create a request and then see it in my requests
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
    * def uniqueTitle = 'Karate request ' + java.util.UUID.randomUUID()

    Given url baseUrl
    And path 'api', 'requests'
    And header Authorization = 'Bearer ' + residentToken
    And request
      """
      {
        "title": "#(uniqueTitle)",
        "category": "PLUMBING",
        "description": "Created by Karate test"
      }
      """
    When method post
    Then status 200
    And match response.id == '#number'
    And match response.task == uniqueTitle
    And match response.status == 'NEW'

    * def createdId = response.id

    Given url baseUrl
    And path 'api', 'requests', 'my'
    And header Authorization = 'Bearer ' + residentToken
    When method get
    Then status 200
    And match response == '#[]'
    And match response[*].task contains uniqueTitle
    And match response[*].id contains createdId