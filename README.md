# cbam-registration-backend

This is the backend repository for the Carbon Border Adjustment Mechanism (CBAM) team's registration journey.

## What this service does

- REST API endpoints for registration data
- Data retrieval from MongoDB
- Integration with HMRC downstream services (e.g. ETMP) for
    - Registration submission
    - Retrieval of user and contact details

### Running the service locally

Prerequisites:

- Java 21
- SBT
- MongoDB
- Service Manager

Commands:

Start CBAM services in service manager:

```
sm2 --start CBAM_ALL
```

Stop this service in service manager:

```
sm2 --stop CBAM_REGISTRATION_BACKEND
```

Run CBAM_REGISTRATION_BACKEND locally using sbt to test dev changes:

```
sbt run
```

To run this service locally with test-only routes enabled:

```
sbt "run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"
```

### Service manager and port info

Service manager: CBAM_REGISTRATION_BACKEND

Port: 11011

### How to sign in locally and on staging

Local:
http://localhost:9949/auth-login-stub/gg-sign-in?continue=http://localhost:11010/cbam/registration

Staging:
https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in?continue=/cbam/registration

### Auth wizard setup for registration

- CredID to be specified, otherwise a random value will be used
- Affinity Group: Organisation
- Credential role: User
- Possible scenarios for enrolments:
    - No enrolments
    - Enrolment with EORI but not CDS or CBAM
    - CDS enrolment but no CBAM enrolment
    - CDS and CBAM enrolments

CDS enrolment details: HMRC-CUS-ORG, EORI, <EORI number, e.g. GB12345612345678>

CBAM enrolment details: HMRC-CBAM-ORG, EORI, <EORI number, e.g. GB12345612345678>

### Running tests

Run unit tests:

```
sbt test
```

Run Integration Tests:

```
sbt it/test
```

Run Unit and Integration Tests with coverage report:

```
sbt clean compile scalafmtAll coverage test it/test coverageReport 
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").