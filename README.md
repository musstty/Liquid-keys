# LiquidKeys — Android keyboard prototype

LiquidKeys is an Android Input Method Editor (IME) prototype with a Liquid Glass-inspired look. This is an **Android Studio source project ZIP**, not an installable APK. A ZIP containing source code cannot be installed directly on a phone; build the project into an APK first.

## Included in this first build
- Real Android keyboard service, selectable in Android Settings
- Four glass themes: Aurora, Pure Glass, Midnight, Neon
- Key opacity, keyboard height, and key font-size settings
- Optional gallery image as keyboard background
- Number row, shift/caps, backspace, symbols toggle, space, enter/action key
- Emoji quick key, clipboard paste, voice typing via Android's available speech-recognition app
- Optional haptic feedback and typing sounds
- One-handed mode setting saved in preferences (layout behavior still needs implementation)
- Local settings storage; no analytics or keystroke collection implemented

## Current prototype limitations
This is an early foundation, not yet a feature-complete Gboard replacement. Word prediction, robust autocorrect, swipe typing, GIF search/sharing, multilingual dictionaries, actual one-handed/floating layouts, and deeper font selection still need to be implemented. The gallery background is currently drawn with basic transparency, not a true live blur. Voice typing relies on an installed Android speech-recognition handler and its own privacy/availability.

## Build an APK
1. Install **Android Studio** on a computer (Windows, macOS, or Linux), including Android SDK Platform 35.
2. Extract this ZIP.
3. Open the extracted `LiquidKeys` folder in Android Studio and let Gradle sync.
4. Choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Android Studio will show the APK location, usually `app/build/outputs/apk/debug/app-debug.apk`.
6. Transfer that APK to your Android phone and open it. If prompted, allow installation from that source.
7. Open LiquidKeys, tap **Enable LiquidKeys**, enable it in Android keyboard settings, then return and tap **Choose LiquidKeys keyboard**.

## Requirements
- Android Studio with JDK 17+ (Android Studio's bundled JDK is recommended)
- Android SDK Platform 35
- Minimum Android version: Android 8.0 (API 26)

## Privacy note
As with any third-party keyboard, use caution when entering passwords or other sensitive data. Voice recognition is handled by the speech-recognition app/service available on the device.
