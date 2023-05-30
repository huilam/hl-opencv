# hl-opencv
OpenCV multi-version jar+native loader and convenience utility methods.

Tested on OpenCV 
- 4.7.0
- 4.6.0

# Enable Camera & Microphone Access for MacOS Eclipse

1. vi Eclipse.app/Contents/Info.plist
```
<key>NSCameraUsageDescription</key>
<string>Eclipse requires camera access</string>
<key>NSMicrophoneUsageDescription</key>
<string>Eclipse requires microphone access</string>
```
2. sudo codesign --force --deep --sign - Eclipse.app
