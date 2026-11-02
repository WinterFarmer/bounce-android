# 🔴 Bounce Classic — Android Native Port

A faithful **Native Android** port of the legendary Nokia J2ME game **Bounce** (`com.nokia.mid.appl.boun`). This project rewrites and migrates the original J2ME game logic, physics calculations, and binary level parser—based on the decompiled J2ME source code from **[rndtrash/nokia-bounce-decomp](https://github.com/rndtrash/nokia-bounce-decomp)**—to modern Android architecture (`SurfaceView` & `Canvas`) using **pure Java with zero third-party libraries or game engines**.

Thanks to R8 optimization (*code shrinking & obfuscation*) and zero external dependencies, the compiled APK is ultra-lightweight — **only ~99 KB!**

---

## ✨ Key Features

- **100% Authentic Physics & Original Levels:** Retains the exact pixel-perfect collision matrices (normal ball, big ball, diagonal slopes, rubber walls, static thorns, and moving thorns) and directly parses the original binary level files (`J2MElvl.001` to `J2MElvl.011`).
- **Crisp Pixel-Art Rendering:** Uses a `128x128` *Virtual Canvas* with *Nearest-Neighbor scaling* (`FilterBitmap = false`) to keep the retro pixel art razor-sharp across all screen resolutions.
- **3 Control Modes (Touch & Physical Hardware):**
  - **Simple (3 Buttons):** Ergonomic Left, Right, and Jump on-screen buttons with multi-touch support.
  - **Numeric Keypad:** Classic 12-button Nokia on-screen keypad layout (`1–9`, `*`, `0`, `#`) for authentic nostalgia and entering cheat codes.
  - **Physical Keypad / Keyboard *(New in v1.1)*:** Full hardware key support for keypad-equipped Android phones (e.g., Smartfren Andromax Prime) and external keyboards. Hides all on-screen touch overlays and automatically centers the game canvas.
- **Full D-Pad Menu Navigation *(New in v1.1)*:** Navigate Main Menu, Level Select, Settings, and Pause dialogs seamlessly using physical directional keys and Enter/Center D-Pad with visual focus highlighting.
- **Broad Android Compatibility *(API 19+)*:**
  - Supports **Android 4.4 KitKat (API 19)** up to the latest Android versions (via the `legacy-kitkat` branch).
  - Seamless **Portrait** and **Landscape** orientation support, plus a toggleable **Fullscreen Mode** with Display Cutout (Notch) protection.
- **Classic Audio & Auto-Save:**
  - Low-latency sound effects (`pop`, `pickup`, `up`) powered by Android `SoundPool` (with automatic legacy `AudioManager.STREAM_MUSIC` fallback for KitKat devices).
  - Automatic persistence for *High Score*, *Unlocked Levels* (1–11), and user preferences via `SharedPreferences`.

---

## ⌨️ Physical Keypad & Keyboard Controls (v1.1+)

You can play using a physical T9 keypad, D-Pad, or QWERTY keyboard right away. Select **PHYSICAL KEYPAD** in **Settings** to hide on-screen touch buttons and center the display.

| Action | Physical Key / Keypad Mapping |
| :--- | :--- |
| **Move Left** | `D-Pad Left`, Keypad `4`, or `A` |
| **Move Right** | `D-Pad Right`, Keypad `6`, or `D` |
| **Jump** | `D-Pad Up`, `D-Pad Center`, Keypad `2`, Keypad `5`, `W`, or `Space` |
| **Menu Navigation** | `D-Pad Up / Down / Left / Right` or Keypad `2 / 8` |
| **Select / Confirm** | `Enter`, `D-Pad Center`, Keypad `5`, or `Space` |
| **Pause / Back** | `Back Button` or `Escape` |

---

## 🕹️ Classic Cheat Codes

During gameplay, type the following key sequences using either the on-screen **NUMERIC KEYPAD** or your **Physical Keypad / Keyboard**:

| Keypad Sequence | Cheat Effect |
| :--- | :--- |
| **`787898`** | **God Mode:** Invincibility against static thorns and moving enemies. |
| **`787899`** | **Developer Cheat Mode:** Unlocks the following shortcut keys: |
| ↳ Press **`1`** | Warp to the **Previous Level**. |
| ↳ Press **`3`** | Warp to the **Next Level**. |
| ↳ Press **`#`** | Activate **Gravity Power-Up** (inverted gravity / fly). |

---

## 📂 Project Architecture

The decompiled J2ME codebase (`b.java`, `d.java`, `e.java`, `f.java`) has been refactored and rewritten for the Android environment into a clean, modular structure:

    app/src/main/
     ├── assets/
     │    ├── icons/                  # Original sprite sheet (objects_nm.png) & splash screens
     │    ├── levels/                 # Original binary level data (J2MElvl.001 - J2MElvl.011)
     │    └── sounds/                 # Game sound effects (.ogg)
     │
     └── java/com/android/bounce/
          ├── MainActivity.java       # Activity lifecycle, Immersive Fullscreen, & Hardware KeyEvent routing
          ├── GameView.java           # SurfaceView, Game Loop (~25 FPS), 3D Bevel UI, Touch & Keypad Input
          ├── core/
          │    ├── Ball.java          # Ball physics, gravity, water buoyancy, & collision matrices (ex-f.java)
          │    ├── LevelLoader.java   # J2ME binary level format parser
          │    ├── LevelData.java     # Map data model, tile matrix, & moving thorn coordinates
          │    ├── TileIDs.java       # Game world tile ID constants
          │    ├── HoopLookup.java    # Sprite lookup tables for hoops/rings (ex-d.java)
          │    ├── SoundManager.java  # Low-latency SoundPool manager (API 19+ & API 21+ compatible)
          │    └── Vec2S.java         # 2D short coordinate vector structure
          └── graphics/
               ├── GameRenderer.java  # Tile map renderer, exit door animation, & pixel-art HUD (ex-b.java)
               └── SpriteSheet.java   # Bitmap slicing, 90/180/270° rotation, & horizontal/vertical flipping

---

## 🌿 Repository Branches

- **`main`:** Standard branch configured for local Android builds (`minSdk 21` / Android 5.0+).
- **`legacy-kitkat`:** Extended compatibility branch lowering `minSdk` to **API 19 (Android 4.4 KitKat)** with legacy `SoundPool` audio fallback and automated GitHub Actions CI build workflow.

---

## 🛠️ Building from Source

1. Clone this repository (use `-b legacy-kitkat` for the KitKat 4.4+ compatible build):

       git clone -b kitkat-legacy https://github.com/WinterFarmer/bounce-android.git
       cd YOUR_REPO_NAME

2. Build the debug APK using the Gradle Wrapper:

       bash gradlew assembleDebug

3. The R8-optimized APK will be generated at:

       app/build/outputs/apk/debug/app-debug.apk

---

## 🙏 Credits & Acknowledgments

- **[rndtrash/nokia-bounce-decomp](https://github.com/rndtrash/nokia-bounce-decomp):** Huge thanks to [@rndtrash](https://github.com/rndtrash) for decompiling and documenting the original J2ME (`MIDP 1.0 / Nokia UI API`) source code and assets, which served as the primary reference for rewriting this Android native port.
- **Nokia Corporation:** Original creator and copyright holder of **Bounce**.

---

## 📜 Disclaimer
This project was created strictly for educational purposes, game preservation, and studying J2ME-to-Android game architecture migration. All original sprites, level designs, and the **Bounce** name are copyrighted properties of **Nokia Corporation**.
