#!/bin/sh
# Validates operator-supplied SSL files and installs them into the Jetty base.
# Called from the Dockerfile when USE_SSL=true. Usage: install-ssl-config.sh <dir>
# <dir> must contain "keystore" and "ssl.ini" (see docs/Docker_Instructions.md).
set -eu

SRC_DIR="$1"
JETTY_BASE_DIR="${JETTY_BASE:-/var/lib/jetty}"

# SHA-256 of the keystore that was previously committed to this repository.
# Its private key is public and must never be deployed.
COMPROMISED_KEYSTORE_SHA256="31769c08f27c970813c400dcea033b380cc7e7b4854293837496a1579906154c"

fail() {
    echo "ERROR: $*" >&2
    echo "ERROR: See docs/Docker_Instructions.md for how to supply your own keystore and ssl.ini." >&2
    exit 1
}

if [ ! -f "$SRC_DIR/keystore" ] || [ ! -f "$SRC_DIR/ssl.ini" ]; then
    fail "USE_SSL=true but 'keystore' and/or 'ssl.ini' were not provided. Pass a directory containing both with: --build-context ssl-config=/path/to/dir"
fi

if [ "$(sha256sum "$SRC_DIR/keystore" | cut -d' ' -f1)" = "$COMPROMISED_KEYSTORE_SHA256" ]; then
    fail "The supplied keystore is the one formerly committed to this repository. Its private key is compromised; generate a new keystore."
fi

# Prints the value of an active (uncommented) property, or nothing if unset.
get_prop() {
    sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*//p" "$SRC_DIR/ssl.ini" | tail -n 1 | sed 's/[[:space:]]*$//'
}

# Rejects empty, placeholder, and well-known default passwords.
check_password() {
    name="$1"
    value="$2"
    case "$value" in
        "") fail "$name is empty in ssl.ini." ;;
        *"<"*|*">"*) fail "$name in ssl.ini is still a placeholder ($value)." ;;
    esac
    case "$(echo "$value" | tr '[:upper:]' '[:lower:]')" in
        password|changeit|changeme|secret|storepwd|keypwd|\
        obf:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4|obf:1u2u1wml1z7s1z7a1wnl1u2g)
            fail "$name in ssl.ini uses a well-known default password. Use a unique password for each deployment." ;;
    esac
}

# Jetty falls back to the well-known defaults "storepwd" and "keypwd" when these are unset.
for prop in keyStorePassword keyManagerPassword; do
    grep -q "^[[:space:]]*jetty\.sslContext\.$prop[[:space:]]*=" "$SRC_DIR/ssl.ini" \
        || fail "jetty.sslContext.$prop must be set in ssl.ini (Jetty otherwise uses a well-known default)."
    check_password "jetty.sslContext.$prop" "$(get_prop "jetty\.sslContext\.$prop")"
done
if grep -q "^[[:space:]]*jetty\.sslContext\.trustStorePassword[[:space:]]*=" "$SRC_DIR/ssl.ini"; then
    check_password "jetty.sslContext.trustStorePassword" "$(get_prop 'jetty\.sslContext\.trustStorePassword')"
fi

# The keystore is installed at etc/keystore, so ssl.ini must point there.
for prop in keyStorePath trustStorePath; do
    path="$(get_prop "jetty\.sslContext\.$prop")"
    if [ -n "$path" ] && [ "$path" != "etc/keystore" ]; then
        fail "jetty.sslContext.$prop is '$path' in ssl.ini but the keystore is installed at etc/keystore."
    fi
done

# When the password is plain text, confirm it actually opens the keystore.
store_password="$(get_prop 'jetty\.sslContext\.keyStorePassword')"
case "$store_password" in
    OBF:*|MD5:*|CRYPT:*) ;;
    *)
        KEYSTORE_PASSWORD="$store_password" keytool -list -keystore "$SRC_DIR/keystore" \
            -storepass:env KEYSTORE_PASSWORD > /dev/null 2>&1 \
            || fail "The keystore could not be opened with jetty.sslContext.keyStorePassword from ssl.ini."
        ;;
esac

install -o root -g jetty -m 640 "$SRC_DIR/keystore" "$JETTY_BASE_DIR/etc/keystore"
install -o root -g jetty -m 640 "$SRC_DIR/ssl.ini" "$JETTY_BASE_DIR/start.d/ssl.ini"
echo "Installed operator-supplied keystore and ssl.ini."
