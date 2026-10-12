#!/bin/bash

echo "Install libusb..."
apt-get install build-essential g++ libusb-1.0.0-dev

g++ main.cpp -lusb-1.0 -o android_luks

