# 🔴 Bounce Classic — Android Native Port

A faithful **Native Android** port of the legendary Nokia J2ME game **Bounce** (`com.nokia.mid.appl.boun`). This project migrates the original J2ME game logic, physics calculations, and binary level parser to modern Android architecture (`SurfaceView` & `Canvas`) using **pure Java with zero third-party libraries or game engines**.

Thanks to R8 optimization (*code shrinking & obfuscation*) and zero external dependencies, the compiled APK is ultra-lightweight — **only ~96 KB!**

---

## ✨ Key Features

- **100% Authentic Physics & Original Levels:** Retains the exact pixel-perfect collision matrices (normal ball, big ball, diagonal slopes, rubber walls, static thorns, and moving thorns) and directly parses the original binary level files (`J2MElvl.001` to `J2MElvl.011`).
- **Crisp Pixel-Art Rendering:** Uses a `128x128` *Virtual Canvas* with *Nearest-Neighbor scaling* (`FilterBitmap = false`) to keep the retro pixel art razor-sharp on modern high-resolution screens.
- **Dual Touch Control Modes:**
  - **Simple (3 Buttons):** Ergonomic Left, Right, and Jump on-screen buttons with multi-touch support.
  - **Numeric Keypad:** Classic 12-button Nokia keypad layout (`1–9`, `*`, `0`, `#`) for authentic nostalgia and entering classic cheat codes.
- **Modern Screen Adaptability:**
  - Seamless **Portrait** and **Landscape** orientation support.
  - Toggleable **Fullscreen Mode** with automatic **Display Cutout (Notch/Punch-hole) safe-area protection**.
- **Classic Audio & Auto-Save:**
  - Low-latency sound effects (`pop`, `pickup`, `up`) powered by Android `SoundPool`.
  - Automatic persistence for *High Score*, *Unlocked Levels* (1–11), and user preferences via `SharedPreferences`.

---

## 🕹️ Classic Cheat Codes (Numeric Keypad Mode)

Switch the controller mode to **NUMERIC KEYPAD** in the **Settings** menu, then type the following key sequences during gameplay:

| Keypad Sequence | Cheat Effect |
| :--- | :--- |
| **`787898`** | **God Mode:** Invincibility against static thorns and moving enemies. |
| **`787899`** | **Developer Cheat Mode:** Unlocks the following shortcut keys: |
| ↳ Press **`1`** | Warp to the **Previous Level**. |
| ↳ Press **`3`** | Warp to the **Next Level**. |
| ↳ Press **`#`** | Activate **Gravity Power-Up** (inverted gravity / fly). |

---

## 📂 Project Architecture

The decompiled J2ME codebase—originally split across obfuscated classes (`b.java`, `d.java`, `e.java`, `f.java`)—has been refactored into a clean, modular structure:

    app/src/main/
     ├── assets/
     │    ├── icons/                  # Original sprite sheet (objects_nm.png) & splash screens
     │    ├── levels/                 # Original binary level data (J2MElvl.001 - J2MElvl.011)
     │    └── sounds/                 # Game sound effects (.ogg)
     │
     └── java/com/android/bounce/
          ├── MainActivity.java       # Activity lifecycle, Immersive Fullscreen, & orientation handling
          ├── GameView.java           # SurfaceView, Game Loop (~25 FPS), 3D Bevel UI Menus, & Touch Input
          ├── core/
          │    ├── Ball.java          # Ball physics, gravity, water buoyancy, & collision matrices (ex-f.java)
          │    ├── LevelLoader.java   # J2ME binary level format parser
          │    ├── LevelData.java     # Map data model, tile matrix, & moving thorn coordinates
          │    ├── TileIDs.java       # Game world tile ID constants
          │    ├── HoopLookup.java    # Sprite lookup tables for hoops/rings (ex-d.java)
          │    ├── SoundManager.java  # Low-latency audio manager using SoundPool
          │    └── Vec2S.java         # 2D short coordinate vector structure
          └── graphics/
               ├── GameRenderer.java  # Tile map renderer, exit door animation, & pixel-art HUD (ex-b.java)
               └── SpriteSheet.java   # Bitmap slicing, 90/180/270° rotation, & horizontal/vertical flipping

---

## 🛠️ Building from Source

1. Clone this repository:

       git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
       cd YOUR_REPO_NAME

2. Build the debug APK using the Gradle Wrapper:

       bash gradlew assembleDebug

3. The R8-optimized APK will be generated at:

       app/build/outputs/apk/debug/app-debug.apk

---

## 📜 Disclaimer
This project was created strictly for educational purposes, game preservation, and studying J2ME-to-Android game architecture migration. All original sprites, level designs, and the **Bounce** name are copyrighted properties of **Nokia Corporation**.
