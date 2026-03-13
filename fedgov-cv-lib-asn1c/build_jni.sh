#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="${SCRIPT_DIR}/build"
J2735_VERSION="2024"
CLEAN=false
SKIP_DEPS=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            CLEAN=true
            shift
            ;;
        --skip-deps)
            SKIP_DEPS=true
            shift
            ;;
        --j2735-version)
            J2735_VERSION="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--clean] [--skip-deps] [--j2735-version VERSION]"
            exit 1
            ;;
    esac
done

if [[ "$CLEAN" = true ]] && [[ -d "$BUILD_DIR" ]]; then
    echo "Cleaning build directory..."
    rm -rf "$BUILD_DIR"
fi
mkdir -p "$BUILD_DIR"
cd "$BUILD_DIR"

if [[ "$SKIP_DEPS" = false ]]; then
    echo "deb [trusted=yes] http://s3.amazonaws.com/stol-apt-repository develop focal" > /etc/apt/sources.list.d/stol-apt-repository.list
    apt-get clean
    apt-get update --fix-missing
    apt-get install -y cmake stol-j2735-2024-1
fi

echo "Configuring CMake (J2735 version: ${J2735_VERSION})..."
cmake .. \
    -DSAEJ2735_SPEC_VERSION="${J2735_VERSION}" \
    -DCMAKE_BUILD_TYPE=Release

echo "Building..."
cmake --build . --parallel "$(nproc)"

echo ""
echo "Build complete. Output:"
ls -la "${SCRIPT_DIR}/lib/libasn1c_jni.so"

# Copy the combined .so under all the original library names
for name in libasn1c.so libasn1c_decoder.so libasn1c_timdecoder.so libasn1c_timencoder.so libasn1c_rga.so; do
    cp "${SCRIPT_DIR}/lib/libasn1c_jni.so" "${SCRIPT_DIR}/lib/${name}"
done