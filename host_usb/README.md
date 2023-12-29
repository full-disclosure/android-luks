# host_usb

A program to interact with Android LUKS app.
Executed on host where LUKS needs to be unlocked.

# Update crypttab

The `crypttab` needs to be updated manually using a supplied example (see repo).
Typically, just replace the last `discard` with `keyscript=/usr/local/sbin/openluksdevices.sh`

# Install

Run `./install.sh` to compile the program and install needed scripts in initramfs.

# Init the key

Run the Android app and press "Init" button to generate a random encryption key.

# Add the key to LUKS

Connect your phone over USB and unlock the screen.
Run `./add-key.sh /dev/yourluks`, where `/dev/yourluks` is your encrypted partition, ex, /dev/sda2. You should see the Android app poping up. Press the "Unlock" button to unclock the key. Enter your existing LUKS passphrase and wait until the script finishes.

You can re-run `add-key.sh` script with another phone and add mutiple keys.

# Test

Re-plug the USB, unlock the screen and reboot your computer. See the reference video:

[example.mp4](../docs/example.mp4)

