# Tesla Delivery Checklist Android App

A deliberately simple native Android app based on the supplied Tesla Model 3 delivery-day checklist.

## What it does

- Shows the checklist in sections
- Lets you mark each item as **Pass**, **Issue**, or **N/A**
- Lets you add notes against each item
- Saves progress locally on the phone
- Shows a quick issue summary
- Shares a plain-text handover report via Android's share sheet

## Easiest way to run it on your phone

1. Install **Android Studio** on your computer: https://developer.android.com/studio
2. Open Android Studio.
3. Choose **Open** and select this folder: `tesla-delivery-checklist`.
4. Let Android Studio sync/download Gradle and Android SDK components.
5. On your Android phone:
   - Enable **Developer options**.
   - Enable **USB debugging**.
   - Plug the phone into your computer.
   - Accept the USB debugging prompt on the phone.
6. In Android Studio, choose your phone from the device dropdown.
7. Press the green **Run** button.

The app should install and open on your phone as **Tesla Checklist**.

## To build an APK

In Android Studio:

- Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
- When complete, click **locate** to find the APK.

Or from a machine with Java/Android SDK/Gradle set up:

```bash
gradle assembleDebug
```

The debug APK will be under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Notes

This first version is intentionally simple: no login, no cloud account, no database setup, and no internet requirement.
Checklist progress is stored locally using Android SharedPreferences.
