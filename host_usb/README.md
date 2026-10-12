# host_usb

A program to interact with Android LUKS app.
Executed on host where LUKS needs to be unlocked.

# Update crypttab

## initramfs-tools

The `crypttab` needs to be updated manually using a supplied example (`crypttab.example`).
Typically, just replace the last `discard` with `keyscript=/usr/local/sbin/openluksdevices.sh`

## dracut

Ubuntu 26 defaults to dracut and systemd, and there is no keyscript available.
Instead, we need to add a socket, that will provide the key data. See `crypttab.dracut` example.

# Install

## initramfs-tools

Run `sudo ./install.sh` to compile the program and install needed scripts in initramfs.

## dracut

Run `sudo ./install-dracut.sh` instead.

# Init the key

Run the Android app and press "Init" button to generate a random encryption key.

# Add the key to LUKS

Connect your phone over USB and unlock the screen.
Run `sudo ./add-key.sh /dev/yourluks`, where `/dev/yourluks` is your encrypted partition, ex, /dev/sda2. You should see the Android app poping up. Press the "Unlock" button to unlock the key. Enter your existing LUKS passphrase and wait until the script finishes.

You can re-run `add-key.sh` script with another phone and add mutiple keys.

# Test

Re-plug the USB, unlock the screen and reboot your computer. See the reference video:

[example.mp4](../docs/example.mp4)

