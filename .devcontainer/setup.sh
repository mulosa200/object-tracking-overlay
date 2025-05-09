#!/bin/bash
# Install Android SDK
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-linux-9477386_latest.zip -d cmdline-tools
mkdir -p /usr/local/android-sdk
mv cmdline-tools /usr/local/android-sdk/cmdline-tools
export ANDROID_HOME=/usr/local/android-sdk
echo "export ANDROID_HOME=/usr/local/android-sdk" >> ~/.bashrc
echo "export PATH=$ANDROID_HOME/cmdline-tools/bin:$PATH" >> ~/.bashrc
source ~/.bashrc

# Accept SDK licenses and install tools
yes | /usr/local/android-sdk/cmdline-tools/bin/sdkmanager --licenses
/usr/local/android-sdk/cmdline-tools/bin/sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

# Install dependencies
sudo apt-get update
sudo apt-get install -y unzip libglu1-mesa
