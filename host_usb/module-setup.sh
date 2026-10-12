#!/bin/bash
# Install to: /usr/lib/dracut/modules.d/90addfiles/

SCRIPT="/usr/local/sbin/openluksdevices.sh"
BINARY="/usr/local/sbin/android_luks"

check() {
    return 0
}

depends() {
    echo systemd crypt
    return 0
}

install() {
    inst_script "$SCRIPT" "$SCRIPT" || dfatal "addfiles: failed to install $SCRIPT"
    inst_binary "$BINARY" "$BINARY" || dfatal "addfiles: failed to install $BINARY"

    inst_simple "$moddir/luks-key.socket"   "$systemdsystemunitdir/luks-key.socket"
    inst_simple "$moddir/luks-key@.service" "$systemdsystemunitdir/luks-key@.service"
    $SYSTEMCTL -q --root "$initdir" enable luks-key.socket
}

