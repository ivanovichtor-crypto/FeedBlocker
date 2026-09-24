# FeedBlocker

An Android app that blocks the recommendation feeds in TikTok (For You) and YouTube Shorts — without an internet connection, without an account, and without collecting any data.

Helps you avoid endless scrolling: as soon as the app detects that you've opened a recommendation feed, it automatically backs out of it.

## How it works

FeedBlocker uses a standard Android system mechanism — **AccessibilityService**. This is the same API that powers screen readers for visually impaired users, as well as other digital wellbeing apps (Digital Wellbeing, Opal, One Sec).

The service is restricted (via `accessibility_service_config.xml`) to receive events only from TikTok and YouTube, so it never sees anything happening in other apps. When it detects the "Рекомендации" ("For You") tab in TikTok, it switches you to the Friends tab (or, if that fails, simply goes back); when it detects the YouTube Shorts player container, it goes back immediately.

**Note**: TikTok and YouTube periodically change their internal layout with app updates. If blocking stops working after an update, that's expected — the view IDs and text labels in `FeedBlockerAccessibilityService.kt` need to be updated to match the new version. I'll try to keep it up to date; PRs with updated IDs are very welcome.

## Turning it on and off

There's no manual "protection on/off" switch and no status indicator on the home screen. Blocking is simply **always active** once the Accessibility service is enabled in system settings — that's the entire on/off logic. The home screen only has two things: the "i" button and a pause button.

- **"i" button** — opens a short setup guide with dedicated buttons: **Settings page** (needed first, to allow restricted settings on Android 13+ for sideloaded apps — see below), **Autostart** (best-effort: tries to open the right autostart-management screen for your phone's manufacturer — Xiaomi/HyperOS, Huawei/Honor, Oppo/Realme/OnePlus, Vivo, and a few others — falling back to the app's Settings page if none match), and **Accessibility settings** (required once, Android won't let apps enable this permission programmatically).
- **"Включить ленту на 5 минут" ("Turn feed on for 5 minutes")** — the only way to pause, for when you genuinely need to watch something in the feed. Also available as an action on the persistent notification. While paused, the button becomes "Снять паузу" ("End pause") with a countdown above it.

**Installed the APK manually?** Since it isn't from Google Play, Android 13+ hides the Accessibility toggle for it by default ("For your security, this setting is currently unavailable"). The "i" guide walks through the fix: open the app's Settings page → tap the ⋮ menu in the top-right corner → **"Allow restricted settings"** → confirm. Only after that does the Accessibility toggle become available.

## Privacy & permissions

FeedBlocker does **not collect, store, or transmit** any data from the screen or any personal information. The app doesn't use an internet connection and has no analytics or trackers. All processing happens locally, on the device itself.

It requests only the permissions it actually uses:

| Permission | What it's for |
|---|---|
| `BIND_ACCESSIBILITY_SERVICE` (via the service declaration) | Lets the service read on-screen structure for TikTok/YouTube only, and trigger the Back action |
| `POST_NOTIFICATIONS` | The persistent "protection is active" notification, with a quick "pause 5 min" action |
| `RECEIVE_BOOT_COMPLETED` | Silently restores that notification after a reboot (see below) — it never opens the app UI on its own |

There's no `SYSTEM_ALERT_WINDOW` ("draw over other apps") and no foreground-service permission — earlier builds requested both, but nothing in the code ever used them, so they were dropped.

The entire source code is open and available in this repository — feel free to verify all of this yourself.

## What happens after a reboot

Android automatically re-binds an AccessibilityService that was already enabled in system settings — the app doesn't need to do anything for that. The only thing that doesn't survive a reboot is the ongoing notification (it's a regular notification, not tied to a foreground service), so `BootReceiver` quietly re-shows it if protection should be active. It never opens `MainActivity` on boot.

## Features

- Blocks the "For You" feed in TikTok
- Blocks YouTube Shorts
- One-tap 5-minute pause, from the app or from the notification
- Best-effort autostart setup shortcut for major Android OEMs
- Minimal interface, no status noise — light and dark theme
- Zero network permissions, zero tracking

## Installation

1. Go to the [Releases](../../releases) section
2. Download the latest `.apk` file
3. Install it manually (you may need to allow "Install from unknown sources" for your file manager/browser)
4. Open the app and tap the **i** button for a step-by-step guide
5. Follow the guide in order: allow restricted settings (if installed outside Google Play) → enable Accessibility → set up autostart / remove battery restrictions (especially important on Xiaomi/HyperOS, Huawei/Honor, Oppo, Vivo)

## Tech stack

- Kotlin
- Jetpack Compose (Material 3)
- AccessibilityService API
- No third-party dependencies beyond AndroidX/Compose — no network, no ads/analytics SDKs

## Building from source

```bash
git clone https://github.com/ivanovichtor-crypto/FeedBlocker.git
cd FeedBlocker
./gradlew assembleDebug
```

Requires JDK 17+ and the Android SDK (`compileSdk 36`, `minSdk 26`). Gradle will pull dependencies from Google's and Maven Central's repositories on first run.

## Known limitations

- Detection is based on matching visible text ("Рекомендации", "Друзья") and view IDs, so it currently expects the TikTok/YouTube UI language and layout it was built against; if TikTok/YouTube ship a redesign or a different locale's wording, blocking may stop working until the app is updated.
- Some OEM battery managers (MIUI, EMUI, ColorOS, etc.) can still kill the accessibility service in the background regardless of in-app settings; the app can only point you to the right system screen, not force the OS to comply.

## Feedback

Found a bug, or did blocking stop working after a TikTok/YouTube update? Open an [Issue](../../issues) — I'll try to look into it.

## License

Open source — feel free to use, study, and suggest improvements.
