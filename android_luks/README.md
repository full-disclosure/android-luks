# android-luks

An app that allows secure LUKS unlocking using usb accessory mode

# Build

```
./gradlew "app:assemble"
```

# Release

Always use the latest version from Github Release page.
Releases are signed using self-signed certificate:

```
X.509 Subject: C=LT, ST=Lithuania, L=Lithuania, O=Full-Disclosure.eu, OU=Android, CN=Full Disclosure
Signature Algorithm: rsassa_pkcs1v15
Valid From: 2023-09-02 06:12:56+00:00
Valid To: 2051-01-18 06:12:56+00:00
Issuer: C=LT, ST=Lithuania, L=Lithuania, O=Full-Disclosure.eu, OU=Android, CN=Full Disclosure
Serial Number: 0x58f1b04968639bf7
Hash Algorithm: sha256
md5: 4745f8ac961cd602b562f1142afb1adc
sha1: 937c62119d58c58b30f82a046f57458a3c93d071
sha256: caa3c53fc63e9bd7b41178bc556817a08299f7d14bd994be3506d06301c893d2
sha512: c9a6eb82769a6bf12dde3bd0d6a4eb755a6c471a24a152e54256794d7f5845c7c6dc0d4788c3420915e83cf3f0e322bd01d78bfe4779df0390675fb9adc609a5
```
To verify:
```
apksigner verify --print-certs -verbose android_luks_v*.apk
```

