# SideKeys

SideKeys is a small Android accessibility-service app that puts floating volume + power controls on the left or right edge of the screen. It is designed for phones whose physical buttons are worn or unreliable.

## Features
- Floating + / − / Power controls inspired by the supplied reference image.
- Vertical volume slider with current media-volume number.
- Tap Power = lock screen.
- Long-press Power = Android power menu.
- Left/right placement.
- Adjustable button size and opacity.
- Automatically lifts the control panel above the software keyboard when the IME window is detected.
- Settings are stored locally with SharedPreferences.

## Build
Open the project in Android Studio, let Gradle sync, then Build > Build APK(s). The APK will be under `app/build/outputs/apk/debug/` for a debug build.

## Install
On the phone enable installation from the source you use (if Android asks), install the APK, open SideKeys, tap **Enable SideKeys Accessibility Service**, then enable SideKeys under Accessibility. Return to the app and adjust the controls.

## Important Android limitation
Normal third-party apps cannot completely emulate every hardware power-button behavior. SideKeys uses Android AccessibilityService global actions: Android officially exposes screen-lock and power-dialog actions. See the Android AccessibilityService documentation.
