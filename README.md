# TesSure

TesSure is a simple Android checklist for inspecting a Tesla on collection day.

## Features

- Model 3 and Model Y support
- Separate saved progress for each model
- Pass, Issue, and N/A responses
- Notes and photos for reported issues
- User-created custom checks
- Issue review and report sharing
- Works offline with no account required

## Run the app

Open the project in Android Studio, connect an Android phone with USB debugging enabled, and select **Run**.

## Build an APK

Run:

```powershell
.\gradlew.bat assembleDebug
```

The APK is created at:

```text
app/build/outputs/apk/debug/TesSure-debug-1.0.apk
```

GitHub Actions also builds a downloadable `tessure-debug-apk` artifact after changes are pushed to the `main` branch.
