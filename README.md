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

**This repository is under active development.** The current APK is an early Android interface prototype. Ubuntu execution and a graphical Linux desktop are not implemented yet; see the status notes below.

## Current features

- Native Android launcher with a monochrome Nox interface.
- Settings pages for Touch Controls, Display, Linux Setup and About.
- Touchpad gesture demo with coordinate feedback.
- Adjustable pointer sensitivity, saved between launches.
- Show/hide touchpad preference, saved between launches.
- Portrait, landscape and automatic orientation actions.
- Android soft-keyboard launch.
- Bundled ARM64 PRoot runtime and Ubuntu Base 24.04 root filesystem (verified during build).\n- Offline first-run extraction and interactive Ubuntu shell for ARM64 Android.\n- Automated APK compilation, GitHub Release publishing and generated changelog.

## Download the latest APK

**[Open Releases and download the latest APK](https://github.com/F1N0ZENIX/Project-Nox/releases/latest).**

Each successful push to `main` builds **one APK**, names it `Nox release Y.Y.Y.apk`, attaches that same file to a new GitHub Release, and includes a commit-based changelog. The build workflow cancels superseded runs so rapid commits do not leave several competing builds publishing APKs. The exact version is generated automatically for each run.

## Updating Project Nox

Android permits installing a newer APK over an existing installation only when the package ID matches and both APKs are signed with the same signing key. Project Nox keeps the package ID `com.projectnox.app` and assigns a higher `versionCode` to every automated build.

The workflow now reuses a persistent development keystore from the GitHub Actions cache, so you normally can install the newer APK directly over the previous version without uninstalling it. For a stronger long-term guarantee, configure a permanent keystore using GitHub repository Actions secrets:

1. Create a signing keystore and keep a secure backup. Do not use a production key for this prototype.
2. In GitHub, open **Settings → Secrets and variables → Actions → New repository secret**.
3. Add these four secrets:
   - `NOX_KEYSTORE_BASE64`: the keystore file encoded as one Base64 string.
   - `NOX_KEYSTORE_PASSWORD`: the keystore password.
   - `NOX_KEY_ALIAS`: the key alias.
   - `NOX_KEY_PASSWORD`: the key password.
4. Push a new commit to `main`. The workflow will use that key for subsequent releases.

To encode a keystore file as Base64 on Linux, run `base64 -w 0 nox-release.keystore`. Keep the original keystore and passwords private; anyone with them can sign an APK that Android treats as an update to this app. If Android reports a signature mismatch, the installed APK was signed with a different key; uninstall that old build once, then install the latest Release.

## Ubuntu status

The 0.4 milestone integrates a real **Ubuntu Base 24.04 ARM64 shell** for ARM64 Android devices without root. The build downloads and verifies the pinned PRoot runtime components and the official Ubuntu Base archive, then packages them inside the APK. On first setup, Nox extracts Ubuntu locally, so the root filesystem does not need to be downloaded from inside the app. Allow at least 650 MB free for extraction.

Open **Set up Ubuntu offline → Install Ubuntu offline**, then **Open Ubuntu terminal**. The terminal accepts Linux commands such as `id`, `uname -a`, and `apt-get update`. Internet access is needed only for commands that fetch packages or other network content.

**Important limitation:** this milestone provides the Ubuntu command-line environment, not a finished graphical Linux desktop. XFCE display, an integrated X/VNC bridge, mouse/keyboard forwarding and audio still need to be implemented and tested. The bundled Ubuntu Base is minimal; graphical desktop packages are not preinstalled.

## Roadmap

- [x] Android project and automated debug build
- [x] Touch-first navigation and initial Nox design
- [x] App-window insets fix for devices using edge-to-edge layouts
- [x] Launcher icon and improved README
- [x] Automatic Releases, APK attachment and generated release notes
- [x] Increasing version codes for automated builds
- [x] Reuse a persistent development signing key for automated builds
- [x] Bundle an Android-compatible ARM64 PRoot runtime in the APK\n- [x] Bundle Ubuntu Base 24.04 ARM64 in the APK and verify its checksum during build\n- [x] Offline Ubuntu extraction flow and interactive command-line terminal\n- [x] Start/stop lifecycle for the Ubuntu shell\n- [ ] Improve install cancellation and recovery from interrupted extraction
- [ ] XFCE desktop display integration
- [ ] Forward touch, mouse and keyboard input to Linux
- [ ] Audio and desktop session testing

## Build locally

Requirements: JDK 17 and Android SDK platform 35.

```bash
git clone https://github.com/F1N0ZENIX/Project-Nox.git
cd Project-Nox
gradle --no-daemon assembleDebug
```

APK output from a local build: `app/build/outputs/apk/debug/app-debug.apk`. GitHub Releases rename the single published APK to `Nox release Y.Y.Y.apk`. A local debug build uses the machine's local debug key unless the signing environment variables are configured.

## Device target

- Initial target: ARM64 Android phones, including Nothing Phone (1).
- Minimum Android version: Android 8.0 (API 26).
- Root access is not part of the design.

## Project status and honesty

The touchpad on the current screen is a gesture demo inside the Android app. It does not move a Linux cursor yet. Ubuntu installation, a Linux graphics backend, real input forwarding and audio remain future milestones.

Project Nox is an independent project. It is not affiliated with Nothing or DroidDeck; those names are used only to describe design inspiration.
