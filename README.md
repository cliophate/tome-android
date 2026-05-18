# Tome

Tome is an Android client for [Audiobookshelf](https://github.com/advplyr/audiobookshelf).

This repository is a fork of Lissen, adapted for my own use. It is not affiliated with the official Audiobookshelf app.

## Current Focus

- Tome branding and package namespace
- Clean library and player UI
- Audiobookshelf-backed progress sync
- Offline playback and downloads

## Build

Clone the repository:

```bash
git clone https://github.com/cliophate/tome-android.git
cd tome-android
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

Build a release APK:

```bash
./gradlew assembleRelease
```

The project expects a working Android SDK setup through `local.properties` or your normal Android Studio environment.

## Package Names

- Release: `org.cliophate.tome`
- Debug: `org.cliophate.tome.debug`

## License

Tome is distributed under the MIT License.

This project includes work derived from Lissen by Max Grakov. See `LICENSE` and `NOTICE` for attribution details.
