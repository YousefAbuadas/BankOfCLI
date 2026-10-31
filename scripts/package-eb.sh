#!/usr/bin/env bash
set -Eeuo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
node_pin="$(tr -d '[:space:]' < "$repo_root/.nvmrc")"
db_container="bankofcli-test-$$-${RANDOM}"
db_container_created=false
staging_dir=""

cleanup() {
  if [[ "$db_container_created" == "true" ]]; then
    docker rm --force --volumes "$db_container" >/dev/null 2>&1 || true
  fi
  if [[ -n "$staging_dir" && -d "$staging_dir" ]]; then
    rm -rf -- "$staging_dir"
  fi
}
trap cleanup EXIT

fail() {
  echo "Error: $*" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Required command not found: $1"
}

for command_name in node npm docker openssl curl zip; do
  require_command "$command_name"
done

if [[ -n "${JAVA_HOME:-}" ]]; then
  java_executable="${JAVA_HOME%/}/bin/java"
  [[ -x "$java_executable" ]] || fail "JAVA_HOME does not contain a java executable."
else
  require_command java
  java_executable="$(command -v java)"
fi
javac_executable="${java_executable%/java}/javac"
[[ -x "$javac_executable" ]] || fail "A Java 21 JDK is required (javac was not found)."

java_settings="$("$java_executable" -XshowSettings:properties -version 2>&1)"
java_version="$(sed -n 's/^[[:space:]]*java.specification.version = //p' <<< "$java_settings" | head -n 1)"
[[ "$java_version" == "21" ]] || fail "Java 21 is required (found ${java_version:-unknown})."
javac_version="$("$javac_executable" -version 2>&1)"
[[ "$javac_version" == "javac 21."* ]] || fail "javac 21 is required (found ${javac_version:-unknown})."

actual_node="$(node --version)"
[[ "$actual_node" == "v${node_pin}" ]] || \
  fail "Node ${node_pin} is pinned in .nvmrc (found ${actual_node})."

cd "$repo_root/frontend"
npm ci
npm run lint
npm test -- --runInBand
npm run build

docker info >/dev/null 2>&1 || fail "Docker must be running to create an isolated test database."
test_db_password="$(openssl rand -hex 24)"
test_jwt_secret="$(openssl rand -base64 32)"

docker run --detach \
  --name "$db_container" \
  --env POSTGRES_DB=bankofcli_test \
  --env POSTGRES_USER=bankofcli_test \
  --env "POSTGRES_PASSWORD=${test_db_password}" \
  --publish '127.0.0.1::5432' \
  --health-cmd='pg_isready -U bankofcli_test -d bankofcli_test' \
  --health-interval=1s \
  --health-timeout=5s \
  --health-retries=60 \
  postgres:18-alpine >/dev/null
db_container_created=true

database_port="$(docker port "$db_container" 5432/tcp | awk -F: 'NR == 1 { print $NF }')"
[[ -n "$database_port" ]] || fail "Docker did not publish the test database port."

database_ready=false
for attempt in {1..60}; do
  health_status="$(docker inspect --format '{{.State.Health.Status}}' "$db_container" 2>/dev/null || true)"
  if [[ "$health_status" == "healthy" ]]; then
    database_ready=true
    break
  fi
  if [[ "$health_status" == "unhealthy" ]]; then
    fail "The isolated PostgreSQL test database did not become healthy."
  fi
  sleep 1
done
[[ "$database_ready" == "true" ]] || fail "Timed out waiting for the isolated PostgreSQL test database."

export SPRING_PROFILES_ACTIVE=test
export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:${database_port}/bankofcli_test"
export SPRING_DATASOURCE_USERNAME=bankofcli_test
export SPRING_DATASOURCE_PASSWORD="$test_db_password"
export DB_USERNAME=bankofcli_test
export DB_PASSWORD="$test_db_password"
export JWT_SECRET="$test_jwt_secret"
export JWT_EXPIRATION_MS=28800000

cd "$repo_root"
"$repo_root/backend/mvnw" -f "$repo_root/backend/pom.xml" -Pcloud-package clean verify

app_jar="$(find "$repo_root/backend/target" -maxdepth 1 -type f -name '*.jar' -print -quit)"
[[ -n "$app_jar" ]] || fail "Maven did not produce an executable JAR."

staging_dir="$(mktemp -d "$repo_root/.bankofcli-release.XXXXXX")"
mkdir -p "$staging_dir/certs"
cp "$app_jar" "$staging_dir/application.jar"
cat > "$staging_dir/Procfile" <<'PROCFILE'
web: java -XX:MaxRAMPercentage=50.0 -jar application.jar
PROCFILE
curl --fail --location --silent --show-error \
  https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem \
  --output "$staging_dir/certs/rds-ca-bundle.pem"
[[ -s "$staging_dir/certs/rds-ca-bundle.pem" ]] || fail "The RDS CA bundle download was empty."

(cd "$staging_dir" && zip -q -r "$staging_dir/bankofcli-eb.zip" \
  application.jar Procfile certs/rds-ca-bundle.pem)

release_dir="$repo_root/release"
rm -rf -- "$release_dir"
mv "$staging_dir" "$release_dir"
staging_dir=""

echo "Elastic Beanstalk bundle created at release/bankofcli-eb.zip"
