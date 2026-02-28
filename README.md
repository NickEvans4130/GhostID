# GhostID

A privacy-first Android app for generating and managing complete digital aliases. Each alias is a fully-formed fictional identity with a synthetic face photo, randomised personal details, platform-specific usernames, and cryptographically secure passwords — all stored locally and encrypted at rest.

![screenshot placeholder](docs/screenshot.png)

## Features

- **Alias Generation** — Full fictional identity: name, date of birth, nationality, fake address, phone number, occupation, star sign, blood type, and a generated bio
- **Synthetic Face Photos** — Fetched from [thispersondoesnotexist.com](https://thispersondoesnotexist.com/) and cached locally; falls back to a Dicebear avatar or initials if offline
- **Platform Accounts** — Auto-generated usernames and unique passwords for Signal, Telegram, Discord, WhatsApp, Matrix, Instagram, Twitter/X, Bluesky, Mastodon, TikTok, Reddit, Tumblr, Pinterest, LinkedIn, GitHub, and three privacy-respecting email providers
- **Password Vault** — Searchable list of all passwords across all aliases
- **AES-256-GCM Encryption** — All sensitive fields (passwords, notes, bio) encrypted via Android Keystore before being stored in Room
- **Export / Import** — Encrypted JSON backup of any alias
- **QR Code** — Generate a vCard QR code from alias contact details
- **Clipboard Guard** — Persistent notification when a password is in the clipboard; auto-clears after a configurable timeout
- **Biometric Lock** — Optional app lock via fingerprint or PIN
- **Alias Health Check** — Warns if duplicate usernames exist across aliases
- **Tags and Notes** — Encrypted per-alias notes and custom tags
- **Themes** — Light, Dark, and AMOLED

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room |
| Encryption | Android Keystore + AES-256-GCM |
| Networking | Retrofit + OkHttp |
| Image Loading | Coil (JPEG + SVG) |
| Password Generation | `java.security.SecureRandom` |
| QR Codes | ZXing |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 |

## Build Instructions

### Prerequisites

- Android Studio Hedgehog or newer (or command-line tools)
- Android SDK with platform `android-34` and build tools `34.0.0`
- JDK 17+

### Clone and Build

```bash
git clone git@github.com:NickEvans4130/GhostID.git
cd GhostID
./gradlew assembleDebug
```

The resulting APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Run Tests

```bash
./gradlew test
```

## Privacy

- **No telemetry.** GhostID makes no analytics or tracking requests.
- **No cloud sync.** All data is stored exclusively in local app storage.
- **Cloud backup disabled.** The Android backup system is explicitly opted out in the manifest.
- **Sensitive fields encrypted.** Passwords, notes, and bios are encrypted with AES-256-GCM via the Android Keystore before being written to the database.
- **Clipboard auto-clear.** Copied passwords are cleared from the clipboard after a configurable timeout (15s / 30s / 60s).

The only network requests GhostID makes are:
1. Fetching a synthetic face JPEG from `thispersondoesnotexist.com` when creating a new alias (optional, falls back to a local avatar if unavailable)
2. Loading a Dicebear SVG avatar as a fallback when the face photo is unavailable

## Contributing

Contributions are welcome. Please follow the commit conventions below.

### Commit Message Format (Conventional Commits)

```
<type>(<scope>): <short description>
```

**Types:** `feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `style`, `security`

**Scopes:** `generator`, `ui`, `db`, `crypto`, `network`, `vault`, `settings`, `navigation`, `alias`, `password`

### Branch Strategy

```
main    - stable builds only
dev     - active development
feature/* - individual features, branched from dev
fix/*   - bug fixes
```

## Licence

MIT
