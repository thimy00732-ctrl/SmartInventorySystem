# Smart Inventory Android app

The app is a minimal Kotlin/Jetpack Compose client for authentication, account
profile/password flows, and admin user management.

The debug API URL defaults to `http://10.0.2.2:8080/` for the Android emulator.
Change `API_BASE_URL` in `app/build.gradle.kts` when using a physical device or
another backend host. The manifest permits cleartext traffic for the local
development backend.

Build and run the unit tests from this directory with:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```
