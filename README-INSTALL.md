# SideKeys — no Android Studio build

## Build the APK online with GitHub Actions

1. Create a GitHub account if you don't already have one.
2. Create a new repository named `SideKeys` (a private repository is fine).
3. Upload the contents of this folder to the repository, including `.github/workflows/build-apk.yml`.
4. Commit the files to the `main` branch.
5. Open the repository's **Actions** tab.
6. Open **Build SideKeys APK** and wait for the green check mark.
7. Open the completed workflow run. Under **Artifacts**, download `SideKeys-debug-apk`.
8. Extract the downloaded ZIP. Inside is `app-debug.apk`.
9. Transfer the APK to the Android phone and install it.
10. Open SideKeys and enable its Accessibility Service in Android Settings.

If GitHub asks whether to trust/enable the workflow, enable it and rerun the workflow.
