#!/bin/sh
# Validates operator-supplied SSL files and installs them into the Jetty base.
# Called from the Dockerfile when USE_SSL=true. Usage: install-ssl-config.sh <dir>
# <dir> must contain "keystore" and "ssl.ini" (see docs/Docker_Instructions.md).
set -eu

SRC_DIR="$1"
JETTY_BASE_DIR="${JETTY_BASE:-/var/lib/jetty}"

fail() {
    echo "ERROR: $*" >&2
    echo "ERROR: See docs/Docker_Instructions.md for how to supply your own keystore and ssl.ini." >&2
    exit 1
}

if [ ! -f "$SRC_DIR/keystore" ] || [ ! -f "$SRC_DIR/ssl.ini" ]; then
    fail "USE_SSL=true but 'keystore' and/or 'ssl.ini' were not provided. Pass a directory containing both with: --build-context ssl-config=/path/to/dir"
fi

# Prints the value of an active (uncommented) property, or nothing if unset.
get_prop() {
    sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*//p" "$SRC_DIR/ssl.ini" | tail -n 1 | sed 's/[[:space:]]*$//'
}

# Decodes a Jetty OBF: value (a reversible encoding, not encryption), matching
# org.eclipse.jetty.util.security.Password.deobfuscate. Fails if malformed.
deobfuscate() {
    printf '%s\n' "${1#OBF:}" | LC_ALL=C awk '
        function b36(x,   i, c, n) {
            if (length(x) != 4) exit 1
            n = 0
            for (i = 1; i <= 4; i++) {
                c = index("0123456789abcdefghijklmnopqrstuvwxyz", tolower(substr(x, i, 1)))
                if (c == 0) exit 1
                n = n * 36 + c - 1
            }
            return n
        }
        {
            i = 1
            while (i <= length($0)) {
                if (substr($0, i, 1) == "U") {
                    i++
                    b = int(b36(substr($0, i, 4)) / 256)
                } else {
                    n = b36(substr($0, i, 4))
                    b = int((int(n / 256) + n % 256 - 254) / 2)
                }
                printf "%c", (b + 256) % 256
                i += 4
            }
        }'
}

# Prints the password Jetty will actually use: OBF: values are decoded and
# anything else (including MD5:/CRYPT:) is used as-is.
effective_password() {
    case "$1" in
        OBF:*) deobfuscate "$1" ;;
        *) printf '%s' "$1" ;;
    esac
}

# Rejects empty, placeholder, and well-known default passwords, including
# OBF:-encoded forms of them.
check_password() {
    name="$1"
    value="$2"
    case "$value" in
        "") fail "$name is empty in ssl.ini." ;;
        *"<"*|*">"*) fail "$name in ssl.ini is still a placeholder ($value)." ;;
    esac
    plain="$(effective_password "$value")" || fail "$name in ssl.ini is not a valid OBF: value."
    [ -n "$plain" ] || fail "$name is empty in ssl.ini."
    case "$(printf '%s' "$plain" | tr '[:upper:]' '[:lower:]')" in
        password|changeit|changeme|secret|storepwd|keypwd)
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

# Confirm the password actually opens the keystore.
KEYSTORE_PASSWORD="$(effective_password "$(get_prop 'jetty\.sslContext\.keyStorePassword')")"
export KEYSTORE_PASSWORD
keytool -list -keystore "$SRC_DIR/keystore" -storepass:env KEYSTORE_PASSWORD > /dev/null 2>&1 \
    || fail "The keystore could not be opened with jetty.sslContext.keyStorePassword from ssl.ini."

install -o root -g jetty -m 640 "$SRC_DIR/keystore" "$JETTY_BASE_DIR/etc/keystore"
install -o root -g jetty -m 640 "$SRC_DIR/ssl.ini" "$JETTY_BASE_DIR/start.d/ssl.ini"
echo "Installed operator-supplied keystore and ssl.ini."
