# 🔧 Fix Navigation Drawer Errors

## ❌ Lỗi hiện tại
```
Unresolved reference: navigation
Unresolved reference: User
Unresolved reference: rememberNavController
...
```

## ✅ Đã fix

### 1. **Thêm Navigation Compose dependency**
📁 File: `sis-android/app/build.gradle.kts`

```kotlin
// Navigation Compose
implementation("androidx.navigation:navigation-compose:2.7.7")
```

### 2. **Tạo User model**
📁 File: `sis-android/app/src/main/java/com/sis/android/data/model/User.kt`

```kotlin
package com.sis.android.data.model

data class User(
    val user_id: Int,
    val email: String,
    val fullName: String,
    val role: String,
    val is_active: Boolean
)
```

### 3. **Fix imports trong MainActivityWithDrawer.kt**
Đã thay đổi từ wildcard imports `.*` sang explicit imports để tránh lỗi.

---

## 🚀 Cách sync trong Android Studio

### Bước 1: Sync Gradle
1. Mở Android Studio
2. Click vào **File** → **Sync Project with Gradle Files**
3. Hoặc click vào icon 🐘 "Sync Now" ở góc phải trên thanh thông báo

### Bước 2: Clean & Rebuild
```
Build → Clean Project
Build → Rebuild Project
```

### Bước 3: Invalidate Caches (nếu vẫn lỗi)
```
File → Invalidate Caches / Restart...
→ Chọn "Invalidate and Restart"
```

---

## 🎯 Verify dependencies đã tải

Sau khi sync, check trong `External Libraries` (Project view):
- ✅ `androidx.navigation:navigation-compose:2.7.7`
- ✅ `androidx.navigation:navigation-runtime:2.7.7`
- ✅ `androidx.compose.material3:material3`

---

## 📝 File structure hiện tại

```
sis-android/app/src/main/java/com/sis/android/
├── MainActivityWithDrawer.kt ✅
├── data/
│   └── model/
│       └── User.kt ✅
└── ui/
    └── navigation/
        └── NavigationDrawer.kt ✅
```

---

## 🔍 Nếu vẫn còn lỗi

### Lỗi: "Unresolved reference: navigation"
**Nguyên nhân**: Dependency chưa được sync

**Fix**:
1. Click "Sync Now" trong Android Studio
2. Đợi Gradle sync xong
3. Build lại project

### Lỗi: "Unresolved reference: User"
**Nguyên nhân**: Import path sai

**Fix**: Check import statement:
```kotlin
import com.sis.android.data.model.User
```

### Lỗi compile Kotlin version
**Nguyên nhân**: Navigation 2.7.7 cần Kotlin 1.9+

**Fix**: Đã set trong build.gradle.kts:
```kotlin
composeOptions {
    kotlinCompilerExtensionVersion = "1.5.14"
}
```

---

## ✅ Sau khi fix xong

Test bằng cách:
1. Build project thành công
2. Run app trên emulator
3. Bấm vào hamburger menu ☰
4. Test expand/collapse các sections
5. Test navigate giữa các screens

---

## 📊 Dependencies đầy đủ

```kotlin
dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")

    // ⭐ Navigation Compose (MỚI THÊM)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // ML Kit
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
}
```

---

## 🎉 Kết quả mong đợi

Sau khi fix xong, bạn sẽ có:
- ✅ 0 compile errors
- ✅ Navigation drawer hoạt động
- ✅ Expandable sections với animation mượt
- ✅ Bottom navigation bar
- ✅ 25+ screens ready

---

## 💡 Tips

1. **Luôn sync Gradle** sau khi thêm dependency mới
2. **Clean build** khi có lỗi cache: `Build → Clean Project`
3. **Check External Libraries** để verify dependencies đã tải
4. **Use explicit imports** thay vì wildcard `.*` để dễ debug

---

## 📞 Nếu cần thêm trợ giúp

Gửi lại full error log từ Android Studio Build Output để tôi có thể diagnose chính xác hơn.
