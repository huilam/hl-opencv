# hl-opencv
OpenCV multi-version jar+native loader and convenience utility methods.

Tested on OpenCV 
- 4.7.0
- 4.6.0

# Enable Camera & Microphone Access for MacOS Eclipse

1. vi Eclipse.app/Contents/Info.plist

\<key>NSCameraUsageDescription\</key><br />
\<string>Eclipse requires camera access\</string><br />
\<key>NSMicrophoneUsageDescription\</key><br />
\<string>Eclipse requires microphone access\</string><br />

2. sudo codesign --force --deep --sign - Eclipse.app
