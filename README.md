# Bank of CLI

Bank of CLI is a React frontend and Spring Boot API backed by PostgreSQL. The
deployment plan is in [PLAN.md](PLAN.md).

## Local development

Use Java 21 and a running PostgreSQL server. Create separate databases for local
development and integration tests, using your PostgreSQL administrator account:

```sh
createdb -h localhost -U postgres bankofcli
createdb -h localhost -U postgres bankofcli_test
```

For first-time setup, copy the example settings file inside `backend/`. Start
these commands from the repository root:

```sh
cd backend
cp application-env.properties.example application-env.properties
```

Edit `backend/application-env.properties` to set your PostgreSQL username and
password and replace the sample JWT secret. Generate a private secret with:

```sh
openssl rand -base64 32
```

The settings file is ignored by Git. Keep it in `backend/`: the local profile
imports `./application-env.properties` relative to the application's working
directory.

From `backend/`, start the API on port 8080:

```sh
./mvnw spring-boot:run
```

In another terminal, start from the repository root, install the pinned Node
version from `.nvmrc`, then start the frontend:

```sh
cd frontend
npm ci
npm run dev
```

Vite proxies `/auth`, `/user`, and `/transaction` requests to Spring Boot on
port 8080. The built frontend uses the same relative API paths when Spring Boot
serves it.

## Backend tests

From `backend/`, run the tests against the separate test database:

```sh
mvn test
```

Integration tests activate the `test` profile automatically and import the local
settings for database credentials and application configuration. The URL in this
command applies only to the test run and overrides the development URL in
`application-env.properties`. Use a dedicated test database because the tests
write banking records and the test configuration includes `create-drop`.

## Build the Elastic Beanstalk release

Run this from the repository root with Java 21, Node `22.22.1`, Docker, OpenSSL,
curl, and `zip` installed:

```sh
./scripts/package-eb.sh
```

The script installs frontend dependencies, runs frontend lint/tests/build,
starts a temporary PostgreSQL 18 database for Maven verification, and creates
`release/bankofcli-eb.zip`. The ZIP contains `application.jar`, `Procfile`, and
the public RDS CA bundle under `certs/`.

To check the packaged frontend locally without Vite, start your local PostgreSQL
server. From the repository root, switch to `backend/` so the packaged app finds
your local settings, then launch the JAR:

```sh
cd backend
java -jar ../release/application.jar
```

Open `http://localhost:8080` and exercise login and banking operations against
the local database.
