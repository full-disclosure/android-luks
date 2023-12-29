#!/bin/bash

if [ -z "$1" ]
then
	echo "Usage: $0 /dev/partition_name"
	echo "Example: $0 /dev/nvme0n1p5"
	exit 1
fi

cryptsetup luksDump "$1"
res=$?

if [ "$res" -ne "0" ]
then
	echo "Could't find a valid LUKS partition or access was denied"
	exit 2
fi

echo "Transferring the key from the phone"
KEY=`./android_luks | xxd -p -c 8192`
size=${#KEY}
if [ "$size" -ne "16384" ]
then
	echo "Coulnd't get the key. Please unplug the phone, kill the app and retry"
	exit 3
fi

echo "Adding the key to LUKS partition: \"$1\""
echo $KEY | xxd -r -p > /root/android_luks_key
cryptsetup luksAddKey "$1" /root/android_luks_key
shred /root/android_luks_key
rm /root/android_luks_key

echo "Done"

