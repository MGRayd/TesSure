# TesSure Android App

A deliberately simple native Android app based on the supplied Tesla Model 3 delivery-day checklist.

## What it does

- Opens on a dark themed landing page
- Lets the user choose **Model 3** or **Model Y**
- Shows a real vehicle image for each model on the selection screen
- Keeps separate checklist progress, notes, issues, and photos for each model
- Includes a Custom Checks section for adding model-specific inspection items
- Uses a compact section picker from the **☰** menu, avoiding lots of scrolling
- Groups checks into broader sections such as **Exterior Checks Stage 1**, **Exterior Checks Stage 2**, **Interior & Cabin Tech**, **Access, Charging & Documents**, and **Final Checks**
- Shows a green tick next to completed sections in the dropdown
- Shows a red issue indicator with the number of issues in each affected section
- Shows the first section by default
- Opens the next section automatically when the current section is complete
- Includes a progress bar for the overall checklist
- Uses a compact top-right **☰** menu for section selection, View Issues, Share, Cars, and Reset
- The **☰** menu uses the same dark card styling, red accent, and smooth slide/fade animation as the rest of the app
- Lets you mark each item as **Pass**, **Issue**, or **N/A**
- Only shows the issue details process when an item is marked **Issue**
- Uses a two-step issue process: **Step 1** describe the issue, then **Step 2** add or take a photo
- Includes non-issue reminders to take a nice exterior and interior photo
- Saves progress locally on the phone
- Shows a full issue review page with a dropdown of sections that have issues, issue text, photo thumbnails, and tap-to-view full-size photos
- Exports/sends an issue report via Android share sheet, including attached issue photos where available
- Shares a plain-text handover report via Android's share sheet
- Includes a GitHub Actions workflow that builds a downloadable debug APK

## Easiest way to run it on your phone

1. Install **Android Studio** on your computer: https://developer.android.com/studio
2. Open Android Studio.
3. Choose **Open** and select the project folder.
4. Let Android Studio sync/download Gradle and Android SDK components.
5. On your Android phone:
   - Enable **Developer options**.
   - Enable **USB debugging**.
   - Plug the phone into your computer.
   - Accept the USB debugging prompt on the phone.
6. In Android Studio, choose your phone from the device dropdown.
7. Press the green **Run** button.

The app should install and open on your phone as **TesSure**.

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
app/build/outputs/apk/debug/TesSure-debug-1.0.apk
```

## App name and icon

The app uses:

- App name: **TesSure**
- Launcher icon: custom car/check icon
- APK filename pattern: `TesSure-<build-type>-<version>.apk`

## Build a ready-to-use APK

For your own phone/testing, build a debug APK:

```text
Build > Build Bundle(s) / APK(s) > Build APK(s)
```

For sharing more widely, create a signed release APK in Android Studio:

```text
Build > Generate Signed Bundle / APK > APK
```

Then create or select a keystore, choose the `release` build variant, and finish the wizard.

## GitHub Actions APK download

This repo includes `.github/workflows/android-debug-apk.yml`.

After you push these files to GitHub:

1. Open the repo on GitHub.
2. Go to **Actions**.
3. Open the latest **Build Android debug APK** run.
4. Download the artifact named `tessure-debug-apk`.
5. Unzip it and install the APK on your Android phone.

You may need to allow installation from unknown sources on your phone for the debug APK.

## Notes

This first version is intentionally simple: no login, no cloud account, no database setup, and no internet requirement.
Checklist progress is stored locally using Android SharedPreferences.
