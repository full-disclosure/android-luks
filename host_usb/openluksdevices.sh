#!/bin/sh

# taken from oxygenimpaired (domain is down)
# http://www.oxygenimpaired.com/debian-lenny-luks-encrypted-root-hidden-usb-keyfile

TRUE=0
FALSE=1

# flag tracking key-file availability
OPENED=$FALSE

/usr/local/sbin/android_luks > /tmp/key
SZ=$(stat -c "%s" /tmp/key)
if [ $SZ -eq "8192" ]
then
	OPENED=$TRUE
fi

if [ $OPENED -ne $TRUE ]
then
	echo "FAILED to get USB key file ..." >&2
	/lib/cryptsetup/askpass "Try LUKS password: "
else
	echo "Success loading key file for Root . Moving on." >&2
	cat /tmp/key
fi

sleep 2

