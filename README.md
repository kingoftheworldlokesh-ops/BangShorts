# BangShorts

BangShorts is a small Android app that uses an Accessibility Service to let you watch the first YouTube Short, then navigate back when you move to another Short.

## How it works

- The service only watches the YouTube app (`com.google.android.youtube`).
- It identifies the Shorts screen from the accessibility tree.
- The first Shorts screen in a YouTube session is allowed.
- A subsequent Shorts swipe (`TYPE_VIEW_SCROLLED`) triggers the Back action.
- The allowance resets after leaving Shorts/YouTube.

This is intentionally conservative: YouTube changes its UI frequently, so the Shorts detection code may need updating for a particular YouTube version.

## Build and install

1. Open this repository in Android Studio Ladybug or newer.
2. Let Gradle sync and run the `app` configuration on an Android 8.0+ device.
3. Open BangShorts and tap **Open Accessibility Settings**.
4. Enable **BangShorts** in Android's Accessibility settings.
5. Open YouTube and enter Shorts.

Accessibility services can observe screen content and perform navigation actions. This app does not collect or upload accessibility data.
