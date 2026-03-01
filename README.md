# GhostID

A privacy-first Android app for generating and managing complete digital aliases. Each alias is a fully-formed fictional identity with a real face photo, randomised personal details, platform-specific usernames, cryptographically secure passwords, and a working throwaway inbox — all stored locally and encrypted at rest.

![screenshot placeholder](docs/screenshot.png)

## Features

- **Alias Generation** — Full fictional identity: name, date of birth, nationality, address, phone number, occupation, star sign, blood type, and a generated bio
- **Real Face Photos** — Gender and age-matched photos fetched from [randomuser.me](https://randomuser.me); falls back to [thispersondoesnotexist.com](https://thispersondoesnotexist.com), then a DiceBear avatar if offline
- **Platform Accounts** — Auto-generated usernames and unique passwords for 18 platforms: Signal, Telegram, Discord, WhatsApp, Matrix, Instagram, Twitter/X, Bluesky, Mastodon, TikTok, Reddit, Tumblr, Pinterest, LinkedIn, GitHub, Proton Mail, Tuta Mail, and Disroot
- **Setup Assistant** — Per-alias checklist tracking which accounts have been created; one-tap opens the platform signup page with password pre-copied to clipboard; phone-required platforms flagged automatically
- **Throwaway Inbox** — Real working email inbox created automatically via [mail.tm](https://mail.tm) when a new alias is generated; read incoming emails inside the app with a sandboxed HTML viewer; auto-polls for new messages every 15 seconds while open
- **Password Vault** — Searchable list of all passwords across all aliases
- **AES-256-GCM Encryption** — All sensitive fields (passwords, notes, bio, email credentials) encrypted via Android Keystore before being stored in Room
- **Export / Import** — Encrypted JSON backup of any alias
- **QR Code** — Generate a vCard QR code from alias contact details
- **Clipboard Guard** — Persistent notification when a password is in the clipboard; auto-clears after a configurable timeout
- **Biometric Lock** — Optional app lock via fingerprint or device PIN
- **Alias Health Check** — Warns if duplicate usernames are shared across aliases
- **Tags and Notes** — Encrypted per-alias notes and custom tags
- **Themes** — Light, Dark, and AMOLED

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room (AES-256-GCM encrypted fields) |
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
- JDK 17 (JDK 21+ required if using command-line Gradle — see note below)

### Clone and Build

```bash
git clone git@github.com:NickEvans4130/GhostID.git
cd GhostID
./gradlew assembleDebug
```

The resulting APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

> If you get a JDK version error, prefix the Gradle command with `JAVA_HOME=/path/to/java-21`.

### Run Tests

```bash
./gradlew test
```

## Privacy

- **No telemetry.** GhostID makes no analytics or tracking requests.
- **No cloud sync.** All data is stored exclusively in local app storage.
- **Cloud backup disabled.** The Android backup system is explicitly opted out in the manifest.
- **Sensitive fields encrypted.** Passwords, notes, bios, and email credentials are encrypted with AES-256-GCM via the Android Keystore before being written to the database.
- **Clipboard auto-clear.** Copied passwords are cleared from the clipboard after a configurable timeout.

The only outbound network requests GhostID makes are:

1. `randomuser.me` — fetches an age and gender-matched face photo when creating a new alias
2. `thispersondoesnotexist.com` — fallback face photo if randomuser.me is unavailable
3. `api.dicebear.com` — offline-safe avatar fallback
4. `api.mail.tm` — creates and reads a throwaway email inbox for the alias

All photo files are downloaded and cached locally. No photos are uploaded or shared.

## Contributing

Contributions are welcome. Please follow the commit conventions below.

### Commit Message Format (Conventional Commits)

```
<type>(<scope>): <short description>
```

**Types:** `feat`, `fix`, `chore`, `refactor`, `test`, `docs`, `style`, `security`

**Scopes:** `generator`, `ui`, `db`, `crypto`, `network`, `vault`, `settings`, `navigation`, `alias`, `password`, `email`, `photo`, `deeplink`

### Branch Strategy

```
main      - stable builds only
dev       - active development
feature/* - individual features, branched from dev
fix/*     - bug fixes
```

## Licence

MIT
