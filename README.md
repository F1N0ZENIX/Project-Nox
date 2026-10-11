# Project Nox

**A Linux desktop on Android — no root required.**

Project Nox aims to run an Ubuntu ARM64 userspace on Android and present it through a lightweight, touch-first desktop with a monochrome, dot-inspired interface.

## Goals
- Ubuntu ARM64 userspace, managed without root access.
- XFCE desktop customized for touch.
- AMOLED black interface inspired by the visual language of Nothing OS.
- Touchpad, left/right click actions, pointer-speed preference and soft-keyboard entry point.
- Settings grouped into Touch Controls, Display, Session and About.
- APK builds through GitHub Actions.

## Current status
This first milestone provides the native Android app shell and first-pass UI. The touchpad currently demonstrates touch movement inside the app; it does **not** yet move a real Linux cursor. The Ubuntu/PRoot installer, graphical server/compositor, audio, and input forwarding still need to be integrated and tested.

## Build
Open the **Actions** tab and run **Build Project Nox APK**, or push a commit to `main`. The debug APK will be attached as a workflow artifact after a successful build.

## Device target
Designed initially for ARM64 Android devices such as the Nothing Phone (1). Minimum Android version: Android 8.0 (API 26).

Project Nox is independent and is not affiliated with Nothing or DroidDeck.
