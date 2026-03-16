# WT-project

A Spring Boot web application for managing apartment/building maintenance requests.

The system supports two main roles:
- **Residents** can create maintenance requests, view their own requests, read request details, add comments, and cancel requests.
- **Admins** can view all requests, filter them, open request details, assign staff, update statuses, and add comments.

The frontend is a lightweight single-page app served by Spring Boot static resources, and authentication is handled with **JWT**.

## Tech stack

- **Java 21**
- **Spring Boot 3.4**
- **Spring Web**
- **Spring Security**
- **Spring Data JPA**
- **MySQL** for the main application
- **H2** for automated tests
- **JWT (jjwt)** for stateless authentication
- **Bootstrap + vanilla JavaScript** for the UI
- **jQuery DataTables** for request listings
- **JUnit 5**, **Rest Assured**, **Karate**, and **Selenium** for testing
- **Jenkins** pipeline included

## Features

### Resident features
- Sign in with JWT-based authentication
- Create a new maintenance request
- View a list of their own requests
- Open full request details
- Add comments to their own requests
- Cancel a request

### Admin features
- Sign in and access the admin dashboard
- View all maintenance requests
- Filter requests by status and priority
- Open request details
- Assign requests to staff members
- Update request status
- Add comments to requests

## Project structure

```text
src/main/java/com.tus/
├── config/          # Spring Security and application configuration
├── controllers/     # REST controllers and SPA forwarding
├── db/models/       # JPA entities and enums
├── db/repos/        # Spring Data repositories
├── dtos/            # Request/response DTOs
├── security/        # JWT service and auth filter
├── services/        # Business logic
└── utils/           # DTO/entity mappers

src/main/resources/
├── application.yml  # Main application configuration
├── data.sql         # Seed data
└── static/          # Frontend files (HTML, JS, CSS, views)

src/test/
├── java/            # Unit, integration, API and Selenium tests
└── resources/       # Test configuration and Karate features
```

## Running the application

### Prerequisites
- Java 21
- Maven 3.9+
- MySQL 8+

### 1. Create the database
Create a MySQL database named:

```sql
CREATE DATABASE mrms;
```

### 2. Check the datasource settings
The default configuration in `src/main/resources/application.yml` uses:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mrms
    username: root
    password: root
server:
  port: 8081
```

Update these values if your local MySQL setup is different.

### 3. Start the application

```bash
mvn clean spring-boot:run
```

The app will start on:

```text
http://localhost:8081
```

Login page:

```text
http://localhost:8081/login.html
```

## Demo accounts

The app seeds sample users from `src/main/resources/data.sql`.

### Admin
- **Username:** `sg@admin.com`
- **Password:** `admin123`

### Resident
- **Username:** `john.murphy@gmail.com`
- **Password:** `resident123`

Other resident accounts are also seeded in `data.sql` and use the same resident password.

## Seeded data

On startup, the application creates sample:
- users (`ADMIN`, `RESIDENT`, `STAFF`)
- maintenance requests with different categories, priorities, and statuses

This makes it easy to test the UI without manually creating records first.

## Authentication model

- Login is handled through `POST /api/auth/login`
- A JWT token is returned on success
- The frontend stores the token in `localStorage`
- Protected API calls send the token in the `Authorization: Bearer <token>` header
- `GET /api/user/me` is used by the SPA to load the current user and build the correct menu

## Main API endpoints

### Authentication
- `POST /api/auth/login`
- `GET /api/user/me`

### Resident endpoints
- `POST /api/requests` - create request
- `GET /api/requests/my` - list current resident's requests
- `GET /api/requests/{id}` - get request details
- `POST /api/requests/{id}/comments` - add comment
- `GET /api/requests/{id}/comments` - list comments
- `PATCH /api/requests/{id}/cancel` - cancel request

### Admin endpoints
- `GET /api/admin/requests` - list all requests
- `GET /api/admin/requests/{id}` - get request details
- `PATCH /api/admin/requests/{id}/status` - update status
- `PATCH /api/admin/requests/{id}/assign` - assign staff member
- `GET /api/admin/requests/staff` - list assignable staff
- `POST /api/admin/requests/{id}/comments` - add comment
- `GET /api/admin/requests/{id}/comments` - list comments

## Frontend routing

The UI is a small SPA served from static resources. Key client-side routes include:

### Resident views
- `/resident/create`
- `/resident/my-requests`
- `/resident/requests/:id`

### Admin views
- `/admin/requests`
- `/admin/requests/:id`

The application entry point redirects users based on role:
- admins land on the admin requests view
- residents land on the create request view

## Testing

The project includes several kinds of tests:

- **Unit tests** for DTOs, entities, mappers, services, and controllers
- **Integration tests** for resident/admin request flows
- **REST Assured tests** for API behaviour
- **Karate tests** for JWT/auth scenarios
- **Selenium tests** for selected UI flows

Run all non-UI tests:

```bash
mvn clean verify
```

Run only Selenium tests:

```bash
mvn -Dtest=*SeleniumTest test
```

### Notes on UI tests
- Selenium tests require Chrome/Chromium to be available.
- The Jenkins pipeline excludes Selenium tests by default unless explicitly enabled.
- UI tests are best used as smoke tests for simple flows such as login, navigation, and viewing seeded data.

## Jenkins

A `Jenkinsfile` is included.

Highlights:
- non-UI tests run by default
- UI tests are controlled by the `RUN_UI_TESTS` parameter
- SonarQube analysis can be enabled with `RUN_SONAR`
- JaCoCo reports are published after the build

## Known implementation notes

- The frontend is implemented as a lightweight SPA using static HTML fragments and JavaScript loaded dynamically.
- JWT is stored in `localStorage` for browser-based authentication.
- The application currently uses `spring.jpa.hibernate.ddl-auto=create`, so the schema is recreated on startup.
- Because `data.sql` is loaded on startup, local data will be reset each time the application starts unless the configuration is changed.

## Future improvements

- Add a Maven Wrapper (`mvnw`) for easier CI setup
- Externalise database credentials through environment variables
- Replace CDN-hosted frontend assets with local copies for more predictable CI/UI test execution
- Expand role-based authorisation coverage and API documentation
- Add more stable end-to-end smoke tests for the UI

## Author

Developed as a Web Technologies project for a maintenance request management system.
