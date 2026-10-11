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
  <a href="https://github.com/F1N0ZENIX/Project-Nox/releases"><img src="https://img.shields.io/github/v/release/F1N0ZENIX/Project-Nox?style=for-the-badge&label=latest%20release" alt="Latest release" /></a>
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
- Automated APK compilation, signing, GitHub Release publishing and generated changelog.

## Download the latest APK

**[Open Releases and download the latest APK](https://github.com/F1N0ZENIX/Project-Nox/releases/latest).**

Whenever a push to `main` builds successfully, GitHub Actions automatically creates a new Release, attaches `app-debug.apk`, and generates release notes from the changes since the previous release. The Actions artifact is also kept for convenient testing.

### Updating Project Nox

- Install the newer APK directly over the installed Project Nox; you should not need to uninstall first.
- The package ID remains `com.projectnox.app`, each automated build receives a higher Android `versionCode`, and the builds use the same **development signing key**.
- **Important:** the signing key is intentionally included in this public repository only for this prototype, so automatic upgrades work. These APKs are development builds, not secure production releases. Do not reuse this key for a production app or for sensitive distribution.
- If Android still refuses the update, check that the APK came from this repository's Releases page and that you have not installed a build signed with a different key.

## Roadmap

- [x] Android project and automated debug build
- [x] Touch-first navigation and initial Nox design
- [x] App-window insets fix for devices using edge-to-edge layouts
- [x] Launcher icon and improved README
- [x] Automatic Releases, APK attachment and generated changelog
- [x] Stable development signature and increasing version codes for in-place updates
- [ ] Ubuntu ARM64 userspace bootstrap using PRoot
- [ ] Download/extract progress, cancellation and storage checks
- [ ] Start/stop lifecycle for the Linux environment
- [ ] XFCE desktop display integration
- [ ] Forward touch, mouse and keyboard input to Linux
- [ ] Audio and desktop session testing

## Build locally

Requirements: JDK 17 and Android SDK platform 35.

The repository stores the prototype development keystore as Base64 text. Decode it before building so the local APK uses the same signature and can be updated by later GitHub Releases:

```bash
git clone https://github.com/F1N0ZENIX/Project-Nox.git
cd Project-Nox
base64 --decode .github/nox-dev.keystore.b64 > .github/nox-dev.keystore
gradle --no-daemon assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`. Do not publish this development key as a production signing key.

## Device target

- Initial target: ARM64 Android phones, including Nothing Phone (1).
- Minimum Android version: Android 8.0 (API 26).
- Root access is not part of the design.

## Project status and honesty

The touchpad on the current screen is a gesture demo inside the Android app. It does not move a Linux cursor yet. Ubuntu installation, a Linux graphics backend, real input forwarding and audio remain future milestones.

Project Nox is an independent project. It is not affiliated with Nothing or DroidDeck; those names are used only to describe design inspiration.
