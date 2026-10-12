#!/bin/bash

echo "Compile..."
./compile.sh

echo "Copy local files..."
cp android_luks       /usr/local/sbin/
cp openluksdevices.sh /usr/local/sbin/

echo "Copy files to dracut..."
mkdir -p /usr/lib/dracut/modules.d/90addfiles
install -m 755 module-setup.sh   /usr/lib/dracut/modules.d/90addfiles/
install -m 644 luks-key.socket   /usr/lib/dracut/modules.d/90addfiles/
install -m 644 luks-key@.service /usr/lib/dracut/modules.d/90addfiles/

echo "Regenerating dracut..."
dracut -f --regenerate-all

echo "Checking..."
lsinitrd /boot/initrd.img-$(uname -r) | grep -E 'luks-key|crypttab|usr/local/sbin'
lsinitrd -f etc/crypttab /boot/initrd.img-$(uname -r)

echo "Done"

