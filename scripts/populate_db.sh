#!/usr/bin/env bash
set -euo pipefail

# Populate Aesthetica database (create DB/user, run migration + seed SQL).
# Usage examples:
#   scripts/populate_db.sh
#   scripts/populate_db.sh --dry-run
#   scripts/populate_db.sh --skip-create
#   APP_DB_PASS='secret' ADMIN_DB_PASS='root_secret' scripts/populate_db.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

MIGRATION_SQL="$PROJECT_ROOT/src/main/resources/db_migration_2026_02_20.sql"
SEED_SQL="$PROJECT_ROOT/src/main/resources/db_seed_data.sql"

APP_DB_HOST="${APP_DB_HOST:-localhost}"
APP_DB_PORT="${APP_DB_PORT:-3306}"
APP_DB_NAME="${APP_DB_NAME:-aesthetica}"
APP_DB_USER="${APP_DB_USER:-aesthetica_user}"
APP_DB_PASS="${APP_DB_PASS:-mysql2006}"

ADMIN_DB_USER="${ADMIN_DB_USER:-root}"
ADMIN_DB_PASS="${ADMIN_DB_PASS:-}"

DRY_RUN=false
SKIP_CREATE=false

for arg in "$@"; do
  case "$arg" in
    --dry-run)
      DRY_RUN=true
      ;;
    --skip-create)
      SKIP_CREATE=true
      ;;
    *)
      echo "Unknown option: $arg"
      echo "Use: --dry-run or --skip-create"
      exit 1
      ;;
  esac
done

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Error: '$1' is required but not installed."
    exit 1
  fi
}

mysql_app() {
  MYSQL_PWD="$APP_DB_PASS" mysql \
    --host="$APP_DB_HOST" \
    --port="$APP_DB_PORT" \
    --user="$APP_DB_USER" \
    --database="$APP_DB_NAME" \
    "$@"
}

mysql_admin() {
  if [[ -n "$ADMIN_DB_PASS" ]]; then
    MYSQL_PWD="$ADMIN_DB_PASS" mysql \
      --host="$APP_DB_HOST" \
      --port="$APP_DB_PORT" \
      --user="$ADMIN_DB_USER" \
      "$@"
  elif [[ "$ADMIN_DB_USER" == "root" ]] && command -v sudo >/dev/null 2>&1; then
    sudo mysql "$@"
  else
    mysql --host="$APP_DB_HOST" --port="$APP_DB_PORT" --user="$ADMIN_DB_USER" "$@"
  fi
}

run_or_echo() {
  if [[ "$DRY_RUN" == "true" ]]; then
    echo "[dry-run] $*"
  else
    eval "$@"
  fi
}

require_cmd mysql
require_cmd java

if [[ ! -f "$MIGRATION_SQL" ]]; then
  echo "Error: migration script not found at $MIGRATION_SQL"
  exit 1
fi

if [[ ! -f "$SEED_SQL" ]]; then
  echo "Error: seed script not found at $SEED_SQL"
  exit 1
fi

echo "==> Database target: $APP_DB_NAME on $APP_DB_HOST:$APP_DB_PORT"
echo "==> App user: $APP_DB_USER"

if [[ "$SKIP_CREATE" == "false" ]]; then
  echo "==> Creating database and ensuring app user privileges"
  CREATE_SQL="CREATE DATABASE IF NOT EXISTS \`$APP_DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; \
CREATE USER IF NOT EXISTS '$APP_DB_USER'@'localhost' IDENTIFIED BY '$APP_DB_PASS'; \
GRANT ALL PRIVILEGES ON \`$APP_DB_NAME\`.* TO '$APP_DB_USER'@'localhost'; \
FLUSH PRIVILEGES;"

  if [[ "$DRY_RUN" == "true" ]]; then
    echo "[dry-run] mysql_admin -e \"$CREATE_SQL\""
  else
    mysql_admin -e "$CREATE_SQL"
  fi
else
  echo "==> Skipping database/user creation (--skip-create)"
fi

bootstrap_schema_if_needed() {
  local table_exists
  table_exists="$(mysql_app -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${APP_DB_NAME}' AND LOWER(table_name)='status';")"

  if [[ "$table_exists" == "0" ]]; then
    echo "==> Core tables not found. Bootstrapping schema via Hibernate (hbm2ddl.auto=update)..."

    local classes_dir="$PROJECT_ROOT/target/classes"
    local lib_glob="$PROJECT_ROOT/target/aesthetica/WEB-INF/lib/*"

    if [[ ! -d "$classes_dir" ]]; then
      echo "Error: $classes_dir not found. Build the project first so entity classes are compiled."
      exit 1
    fi

    local tmp_java
    tmp_java="$(mktemp /tmp/aesthetica-schema-bootstrap-XXXXXX.java)"
    cat > "$tmp_java" <<'JAVA'
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SchemaBootstrap {
    public static void main(String[] args) {
        SessionFactory sf = new Configuration().configure().buildSessionFactory();
        sf.close();
        System.out.println("Hibernate schema bootstrap completed.");
    }
}
JAVA

    if [[ "$DRY_RUN" == "true" ]]; then
      echo "[dry-run] java -cp \"$classes_dir:$lib_glob\" $tmp_java"
      rm -f "$tmp_java"
      return
    fi

    java -cp "$classes_dir:$lib_glob" "$tmp_java"
    rm -f "$tmp_java"

    table_exists="$(mysql_app -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${APP_DB_NAME}' AND LOWER(table_name)='status';")"
    if [[ "$table_exists" == "0" ]]; then
      echo "Error: schema bootstrap did not create expected tables."
      exit 1
    fi
  fi
}

bootstrap_schema_if_needed

normalize_table_names() {
  local -a expected_lower=(status product discount city address cart stock seller)

  for table in "${expected_lower[@]}"; do
    local upper_variant="${table^}"
    local lower_exists
    local upper_exists

    lower_exists="$(mysql_app -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${APP_DB_NAME}' AND table_name='${table}';")"
    upper_exists="$(mysql_app -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${APP_DB_NAME}' AND table_name='${upper_variant}';")"

    if [[ "$lower_exists" == "0" && "$upper_exists" != "0" ]]; then
      echo "==> Normalizing table name: ${upper_variant} -> ${table}"
      if [[ "$DRY_RUN" == "true" ]]; then
        echo "[dry-run] RENAME TABLE \`${upper_variant}\` TO \`${table}\`;"
      else
        mysql_app -e "RENAME TABLE \`${upper_variant}\` TO \`${table}\`;"
      fi
    fi
  done
}

normalize_table_names

echo "==> Running migration script"
if [[ "$DRY_RUN" == "true" ]]; then
  echo "[dry-run] mysql_app < $MIGRATION_SQL"
else
  mysql_app < "$MIGRATION_SQL"
fi

echo "==> Running seed script"
if [[ "$DRY_RUN" == "true" ]]; then
  echo "[dry-run] mysql_app < $SEED_SQL"
else
  mysql_app < "$SEED_SQL"
fi

echo "==> Database population completed successfully"

