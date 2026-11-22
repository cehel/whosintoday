#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."

./gradlew --continue \
    :ktlintCheck \
    :shared:testAndroidHostTest \
    :shared:connectedAndroidDeviceTest \
    :shared:iosSimulatorArm64Test

if [[ "$(uname -s)" != Darwin || "$(uname -m)" != arm64 ]]; then
    printf '%s\n' 'Pre-PR checks are incomplete. The iOS simulator tests require a native Apple Silicon macOS environment.' >&2
    exit 1
fi
