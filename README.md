# NEETQuestSaver 📚

> Production-ready Android app for NEET students to instantly capture and save important questions from any online mock test app or website.

---

## ✨ Features

| Feature | Status |
|---|---|
| 🫧 Draggable Floating Bubble (WindowManager) | ✅ |
| 📸 Screen Capture via MediaProjection | ✅ |
| ✂️ Interactive Crop Editor (pure Compose Canvas) | ✅ |
| 💾 Metadata: Subject / Chapter / Category / Tags / Notes | ✅ |
| 🗄️ Room DB + Full NEET 2025/26 Syllabus (65+ chapters) | ✅ |
| 🔍 Search & multi-filter | ✅ |
| 🌗 Dark/Light Mode (Material 3) | ✅ |
| 📤 JSON Export/Import backup | ✅ |
| 🏠 Dashboard with per-subject stats | ✅ |
| 📋 Question detail with inline edit & delete | ✅ |
| 🏷️ Custom category manager with color picker | ✅ |
| ⚙️ Full Settings screen | ✅ |

---

## 🏗️ Architecture

```
MVVM  +  Hilt DI  +  Room  +  Compose Navigation  +  StateFlow

com.neetquest.neetquestsaver/
├── data/
│   ├── dao/           Daos.kt (SavedQuestion, Chapter, Category)
│   ├── database/      NEETQuestDatabase.kt + seed data
│   ├── entity/        SavedQuestion, Chapter, Category
│   └── repository/    QuestionRepository.kt
├── di/                DatabaseModule.kt
├── service/
│   ├── FloatingBubbleService.kt   ← WindowManager overlay
│   └── ScreenCaptureService.kt    ← MediaProjection capture
├── ui/
│   ├── screens/
│   │   ├── home/       HomeScreen, SavedQuestionsScreen
│   │   ├── crop/       CropEditorScreen, SaveQuestionScreen
│   │   ├── detail/     QuestionDetailScreen
│   │   ├── categories/ CategoriesScreen
│   │   └── settings/   SettingsScreen
│   ├── theme/          Theme.kt, Typography.kt
│   ├── MainActivity.kt  (NavHost + BottomBar)
│   ├── CaptureActivity.kt
│   └── Screen.kt
├── utils/
│   ├── ImageStorageManager.kt
│   ├── PreferencesManager.kt
│   └── BackupManager.kt
└── viewmodel/
    ├── MainViewModel.kt
    └── CropViewModel.kt
```

---

## 🚀 Setup in Android Studio

1. Unzip project → **File → Open** → select `NEETQuestSaver/`
2. Wait for Gradle sync to finish
3. **Run → Run 'app'** on a device or emulator (API 26+)
4. Go to **Settings → Enable Floating Bubble**
5. Grant **Draw Over Other Apps** permission
6. Switch to any mock test app and tap the purple bubble!

---

## 📱 Capture Flow

```
Any App  ──tap bubble──►  CaptureActivity  ──MediaProjection──►
  ScreenCaptureService  ──bitmap──►  CropEditorScreen
    ──crop bitmap──►  SaveQuestionScreen  ──metadata──►  Room DB
```

---

## 🔐 Permissions

| Permission | Purpose |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Floating bubble |
| `FOREGROUND_SERVICE` | Keep bubble alive |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Screen capture service |
| `POST_NOTIFICATIONS` | Foreground notification (API 33+) |
| `READ_MEDIA_IMAGES` | Add from gallery |

---

## 🗃️ Pre-seeded NEET 2025/26 Chapters

- **Physics** — 20 chapters (Kinematics → Electronic Devices)
- **Chemistry** — 28 chapters (Physical + Inorganic + Organic)
- **Botany** — 24 chapters (Living World → Environmental Issues)
- **Zoology** — 13 chapters (Animal Kingdom → Animal Husbandry)

---

## 🧰 Tech Stack

| Library | Version |
|---|---|
| Jetpack Compose BOM | 2024.08.00 |
| Material 3 | - |
| Room | 2.6.1 |
| Hilt | 2.51.1 |
| Coil | 2.7.0 |
| DataStore Preferences | 1.1.1 |
| Navigation Compose | 2.7.7 |
| Gson | 2.11.0 |
| Kotlin Coroutines | 1.8.1 |

---

## ⚠️ Next Steps / TODOs

- Wire file picker for JSON import
- Request `POST_NOTIFICATIONS` at runtime on first launch (Android 13+)
- URI→Bitmap conversion for gallery manual add
- Optional: Widget shortcut on home screen

---

MIT License — for educational use.
