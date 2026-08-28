# FeedBlocker

An Android app that blocks the recommendation feeds in TikTok (For You) and YouTube Shorts.

Helps you avoid endless scrolling: as soon as the app detects that you've opened a recommendation feed, it automatically sends you back to the home screen.

## How it works

FeedBlocker uses a standard Android system mechanism — **AccessibilityService**. This is the same API that powers screen readers for visually impaired users, as well as other digital wellbeing apps (Digital Wellbeing, Opal, One Sec).

The service reads the structure of elements already rendered on screen, detects when the TikTok "For You" feed or YouTube Shorts is open, and in that case triggers the system's Home button action.

**Note**: TikTok and YouTube periodically change their internal layout with app updates. If blocking stops working after an update — that's expected, it means the resource IDs in the code need to be updated to match the new version. I'll try to keep it up to date.

## Privacy

FeedBlocker does **not collect, store, or transmit** any data from the screen or any personal information. The app doesn't use an internet connection and has no analytics or trackers. All processing happens locally, on the device itself.

The entire source code is open and available in this repository — feel free to verify this yourself.

## Features

- Blocks the "For You" feed in TikTok
- Blocks YouTube Shorts
- Simple, minimal interface

## Installation

1. Go to the [Releases](../../releases) section
2. Download the latest `.apk` file
3. Install it manually (you may need to allow "Install from unknown sources" for your file manager/browser)
4. Open the app → enable the AccessibilityService in system settings (the app will guide you)
5. For stable operation on some devices (especially Xiaomi/MIUI/HyperOS), you may need to disable battery restrictions for the app — there's a shortcut for this right inside the app

## Tech stack

- Kotlin
- Jetpack Compose
- AccessibilityService API

## Feedback

Found a bug, or did blocking stop working after a TikTok/YouTube update? Open an [Issue](../../issues) — I'll try to look into it.

## License

Open source — feel free to use, study, and suggest improvements.
