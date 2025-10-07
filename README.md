# android-luks

An app that allows secure LUKS unlocking using usb accessory mode without typing your LUKS password.

Current status is: `stable proof of concept`.

You can use this app, however you may still see crashes as not all errors are handled at the moment.

![CodeQL status](https://github.com/full-disclosure/android-luks/actions/workflows/codeql.yml/badge.svg)


# Supported devices

The app has been tested on:

* Xiaomi A3 (Android 11)
* Google Pixel 3 (Android 12)
* Samsung A54 (Android 13)

The usb host program has been tested on:

* Debian 11 bullseye i586
* Ubuntu 22.04 jammy x86_64

The usb host does not work so far on:

* Linux Mint 21.2 victoria - /etc/crypttab is ignored because systemd is used. The support may be added later


# How to use

Install the Android app, install the Linux host program and scripts,
initialize the key and plug your phone over USB.
Now, when your Linux machine boots, you should see the Android app automatically popping up.
As soon as you press "Unlock" button and confirm your biometrics,
the boot process will continue without typing in the password.

See the reference video:

[example.mp4](docs/example.mp4)

![example.gif](docs/example.gif)


# Android app

The app has two functions: Init and Unlock.

`Init` creates a new random encryption key and securely encrypts it using biometrics.

`Unlock` gets biometrics, decrypts the key and sends it over USB.

Android app sources and manual: [android_luks/](android_luks/)


# Linux host program

The host application consists of a program that communicates with Android over USB and
a LUKS helper script that supplies the decryption key.
The `crypttab` needs to be updated manually using a supplied example.
If LUKS is unable to get the key from USB, it will fall back to your existing passphrase.

Linux host code and manual: [host_usb/](host_usb/)
