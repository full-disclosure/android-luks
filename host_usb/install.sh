#!/bin/bash

echo "Install libusb..."
apt-get install libusb-1.0.0-dev
echo "Compile..."
./compile.sh

echo "Copy files to initramfs..."
cp android_luks       /usr/local/sbin/
cp openluksdevices.sh /usr/local/sbin/
cp android_luks       /usr/local/sbin/
cp crypto.hook        /etc/initramfs-tools/hooks/

echo "Rebuild initramfs..."
update-initramfs -u -v | grep luks

echo "Done"

