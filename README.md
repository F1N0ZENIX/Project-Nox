# PROJECT NOX

<p align="center">
  <img src="docs/nox-banner.svg" alt="Project Nox" width="100%" />
</p>

<p align="center">
  <strong>A touch-first Linux desktop for Android. No root required.</strong><br/>
  Ubuntu ARM64 · XFCE goal · AMOLED-black interface
</p>

<p align="center">
  <a href="https://github.com/F1N0ZENIX/Project-Nox/actions"><img src="https://img.shields.io/badge/build-GitHub%20Actions-black?style=for-the-badge&logo=githubactions&logoColor=white" alt="GitHub Actions" /></a>
  <img src="https://img.shields.io/badge/platform-Android%20ARM64-white?style=for-the-badge&logo=android&logoColor=black" alt="Android ARM64" />
  <img src="https://img.shields.io/badge/license-TBD-555555?style=for-the-badge" alt="License TBD" />
</p>

## The idea

Project Nox aims to bring a real Linux desktop environment to Android without replacing Android or requiring root. Its visual direction is inspired by monochrome industrial design, AMOLED black, dot patterns and compact touch-friendly settings.

**This repository is under active development.** The current APK is an early Android interface prototype; it does not yet install or run Ubuntu.

## Current features

- Native Android launcher with a monochrome Nox interface.
- Settings pages for Touch Controls, Display, Linux Setup and About.
- Touchpad gesture demo with coordinate feedback.
- Adjustable pointer sensitivity, saved between launches.
- Show/hide touchpad preference, saved between launches.
- Portrait, landscape and automatic orientation actions.
- Android soft-keyboard launch.
- GitHub Actions workflow to compile a debug APK.

## Roadmap

- [x] Android project and automated debug build
- [x] Touch-first navigation and initial Nox design
- [x] App-window insets fix for devices using edge-to-edge layouts
- [x] Launcher icon and improved README
- [ ] Ubuntu ARM64 userspace bootstrap using PRoot
- [ ] Download/extract progress, cancellation and storage checks
- [ ] Start/stop lifecycle for the Linux environment
- [ ] XFCE desktop display integration
- [ ] Forward touch, mouse and keyboard input to Linux
- [ ] Audio and desktop session testing

## Get the APK

1. Open the [Actions page](https://github.com/F1N0ZENIX/Project-Nox/actions).
2. Select the latest successful **Build Project Nox APK** run.
3. Scroll to **Artifacts** and download `Project-Nox-debug`.
4. Extract the ZIP and install `app-debug.apk` on your Android device.

Debug artifacts are temporary workflow downloads, not a signed Play Store release.

## Build locally

Requirements: JDK 17 and Android SDK platform 35.

```bash
git clone https://github.com/F1N0ZENIX/Project-Nox.git
cd Project-Nox
gradle --no-daemon assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Device target

- Initial target: ARM64 Android phones, including Nothing Phone (1).
- Minimum Android version: Android 8.0 (API 26).
- Root access is not part of the design.

## Project status and honesty

The touchpad on the current screen is a gesture demo inside the Android app. It does not move a Linux cursor yet. Ubuntu installation, a Linux graphics backend, real input forwarding and audio remain future milestones.

Project Nox is an independent project. It is not affiliated with Nothing or DroidDeck; those names are used only to describe design inspiration.
