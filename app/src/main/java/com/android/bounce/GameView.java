package com.android.bounce;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.android.bounce.core.Ball;
import com.android.bounce.core.GameContext;
import com.android.bounce.core.LevelData;
import com.android.bounce.core.LevelLoader;
import com.android.bounce.core.SoundListener;
import com.android.bounce.core.SoundManager;
import com.android.bounce.core.Vec2S;
import com.android.bounce.graphics.GameRenderer;
import com.android.bounce.graphics.SpriteSheet;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable, GameContext {
    public static final int STATE_SPLASH_NOKIA = -2;
    public static final int STATE_SPLASH_BOUNCE = -1;
    public static final int STATE_MENU = 0;
    public static final int STATE_LEVEL_SELECT = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_LEVEL_COMPLETE = 4;
    public static final int STATE_GAME_OVER = 5;
    public static final int STATE_HIGH_SCORE = 6;
    public static final int STATE_SETTINGS = 7;

    public static final int CONTROL_SIMPLE = 0;
    public static final int CONTROL_NUMPAD = 1;
    public static final int CONTROL_KEYBOARD = 2;

    public int gameState = STATE_SPLASH_NOKIA;
    public int controlType = CONTROL_SIMPLE;
    private boolean isFullscreen = false;
    private int settingsReturnState = STATE_MENU;

    private int menuCursorIndex = 1; 

    private boolean keyLeftHeld = false;
    private boolean keyRightHeld = false;
    private boolean keyJumpHeld = false;

    private int splashTimer = 0;
    private Bitmap splashNokia;
    private Bitmap splashBounce;

    private Thread gameThread;
    private volatile boolean isRunning = false;
    private final SurfaceHolder holder;

    private Bitmap virtualBitmap;
    private Canvas virtualCanvas;
    private Matrix scaleMatrix;
    private Paint screenPaint;

    private GameRenderer renderer;
    private SoundManager soundManager;
    private LevelData currentLevel;
    private Ball ball;

    private int score = 0;
    private int lives = 3;
    private int hoopsScored = 0;
    private int currentLevelId = 1;
    private boolean exitUnlocked = false;
    private int exitOffset = 0;
    private boolean hasActiveGame = false;

    private boolean godMode = false;
    private boolean cheatMode = false;
    private int cheatSequence = 0;

    private int highScore = 0;
    private int maxUnlockedLevel = 1;
    private SharedPreferences prefs;

    private int camX = 0;
    private int camY = 0;

    private final RectF btnLeft = new RectF();
    private final RectF btnRight = new RectF();
    private final RectF btnJump = new RectF();

    private final RectF[] btnNumpad = new RectF[12];
    private final String[] numpadLabels = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"};

    private final RectF btnMenuContinue = new RectF();
    private final RectF btnMenuNewGame = new RectF();
    private final RectF btnMenuLevelSelect = new RectF();
    private final RectF btnMenuHighScore = new RectF();
    private final RectF btnMenuSettings = new RectF();

    private final RectF[] btnLevels = new RectF[11];
    private final RectF btnBack = new RectF();
    private final RectF btnDialogPrimary = new RectF();
    private final RectF btnDialogSecondary = new RectF();
    private final RectF btnDialogSettings = new RectF();

    private final RectF btnSettingSimple = new RectF();
    private final RectF btnSettingNumpad = new RectF();
    private final RectF btnSettingKeyboard = new RectF();
    private final RectF btnSettingFullscreen = new RectF();

    private final Paint uiPaint = new Paint();
    private final Paint borderPaint = new Paint();

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        init();
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    private void init() {
        prefs = getContext().getSharedPreferences("bounce_save", Context.MODE_PRIVATE);
        highScore = prefs.getInt("high_score", 0);
        maxUnlockedLevel = prefs.getInt("max_unlocked_level", 1);
        controlType = prefs.getInt("control_type", CONTROL_SIMPLE);
        isFullscreen = prefs.getBoolean("is_fullscreen", false);

        virtualBitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.RGB_565);
        virtualCanvas = new Canvas(virtualBitmap);
        scaleMatrix = new Matrix();

        screenPaint = new Paint();
        screenPaint.setFilterBitmap(false);
        screenPaint.setAntiAlias(false);

        uiPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3.0f);
        borderPaint.setAntiAlias(false);

        renderer = new GameRenderer(getContext());
        soundManager = new SoundManager(getContext());

        splashNokia = SpriteSheet.loadBitmapFromAsset(getContext(), "icons/nokiagames.png");
        splashBounce = SpriteSheet.loadBitmapFromAsset(getContext(), "icons/bouncesplash.png");

        for (int i = 0; i < 11; i++) btnLevels[i] = new RectF();
        for (int i = 0; i < 12; i++) btnNumpad[i] = new RectF();
    }

    public void startNewGame(int levelId) {
        score = 0;
        lives = 3;
        hasActiveGame = true;
        loadLevel(levelId);
        gameState = STATE_PLAYING;
    }

    public void loadLevel(int id) {
        currentLevelId = id; keyLeftHeld = false; keyRightHeld = false; keyJumpHeld = false;
        currentLevel = LevelLoader.loadLevel(getContext(), id);
        if (currentLevel != null) {
            hoopsScored = 0;
            exitUnlocked = false;
            exitOffset = 0;
            ball = new Ball(currentLevel.ballInitialX * 12 + 6, currentLevel.ballInitialY * 12 + 6, currentLevel.ballSize, this);
            ball.setSprites(renderer.sprites[47], renderer.sprites[49], renderer.sprites[48]);
            ball.setCheckpoint(currentLevel.ballInitialX, currentLevel.ballInitialY);
            updateCamera();
        }
    }

    private void updateCamera() {
        if (ball == null || currentLevel == null) return;
        int px = ball.posX - 64;
        if (px < 0) px = 0;
        int maxW = currentLevel.width * 12 - 128;
        if (px > maxW) px = maxW;
        camX = px;

        while (ball.posY - 6 < camY) camY -= 84;
        while (ball.posY + 6 > camY + 96) camY += 84;
        if (camY < 0) camY = 0;
    }

    private void saveGameProgress() {
        if (score > highScore) highScore = score;
        if (currentLevelId + 1 > maxUnlockedLevel && currentLevelId < 11) {
            maxUnlockedLevel = currentLevelId + 1;
        }
        prefs.edit().putInt("high_score", highScore)
                .putInt("max_unlocked_level", maxUnlockedLevel)
                .putInt("control_type", controlType)
                .putBoolean("is_fullscreen", isFullscreen)
                .apply();
    }

    private void handleKeypadPress(String key) {
        if (ball == null) return;
        switch (key) {
            case "7" -> {
                if (cheatSequence == 0 || cheatSequence == 2) cheatSequence++;
                else cheatSequence = 1;
            }
            case "8" -> {
                if (cheatSequence == 1 || cheatSequence == 3) cheatSequence++;
                else if (cheatSequence == 5) {
                    godMode = !godMode;
                    cheatSequence = 0;
                    if (soundManager != null) soundManager.playUp();
                } else cheatSequence = 0;
            }
            case "9" -> {
                if (cheatSequence == 4) cheatSequence++;
                else if (cheatSequence == 5) {
                    cheatMode = true;
                    cheatSequence = 0;
                    if (soundManager != null) soundManager.playPop();
                } else cheatSequence = 0;
            }
            case "1" -> {
                if (cheatMode) {
                    int prev = (currentLevelId <= 1) ? 11 : currentLevelId - 1;
                    loadLevel(prev);
                }
            }
            case "3" -> {
                if (cheatMode) {
                    int next = (currentLevelId >= 11) ? 1 : currentLevelId + 1;
                    loadLevel(next);
                }
            }
            case "#" -> {
                if (cheatMode) ball.powerUpGravity = 300;
            }
        }
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();
        while (isRunning) {
            long now = System.currentTimeMillis();
            if (now - lastTime >= 40) {
                lastTime = now;
                if (gameState == STATE_SPLASH_NOKIA || gameState == STATE_SPLASH_BOUNCE) {
                    splashTimer++;
                    if (splashTimer >= 38) {
                        splashTimer = 0;
                        if (gameState == STATE_SPLASH_NOKIA) gameState = STATE_SPLASH_BOUNCE;
                        else {
                            gameState = STATE_MENU;
                            menuCursorIndex = hasActiveGame ? 0 : 1;
                        }
                    }
                } else if (gameState == STATE_PLAYING) {
                    updatePhysics();
                    drawVirtual();
                }
                drawToScreen();
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {}
        }
    }

    private void updatePhysics() {
        if (ball != null) {
            if (keyLeftHeld || keyRightHeld || keyJumpHeld) {
                ball.resetAllInputs();
                if (keyLeftHeld) ball.setInput(1);
                if (keyRightHeld) ball.setInput(2);
                if (keyJumpHeld) ball.setInput(8);
            }

            ball.updatePhysics();
            if (ball.state == 1) {
                if (lives <= 0) {
                    saveGameProgress();
                    hasActiveGame = false;
                    menuCursorIndex = 0;
                    gameState = STATE_GAME_OVER;
                    return;
                }
                ball.respawn();
            }
            updateCamera();
        }

        updateDynThorns();

        if (exitUnlocked && exitOffset < 24) {
            exitOffset += 2;
        }
    }

    private void updateDynThorns() {
        if (currentLevel == null || currentLevel.dynThornsCount == 0) return;
        for (int i = 0; i < currentLevel.dynThornsCount; i++) {
            short blX = currentLevel.dynThornsBottomLeft[i].x;
            short blY = currentLevel.dynThornsBottomLeft[i].y;
            short spdX = currentLevel.dynThornsSpeed[i].x;
            short spdY = currentLevel.dynThornsSpeed[i].y;

            if (spdX > 2) spdX = 1; else if (spdX < -2) spdX = -1;
            if (spdY > 2) spdY = 1; else if (spdY < -2) spdY = -1;
            currentLevel.dynThornsSpeed[i].x = spdX;
            currentLevel.dynThornsSpeed[i].y = spdY;

            int maxX = (currentLevel.dynThornsTopRight[i].x - blX - 2) * 12;
            int maxY = (currentLevel.dynThornsTopRight[i].y - blY - 2) * 12;

            if (maxX > 0 && spdX != 0) {
                currentLevel.dynThornsPos[i].x = (short) (currentLevel.dynThornsPos[i].x + spdX);
                if (currentLevel.dynThornsPos[i].x <= 0) {
                    currentLevel.dynThornsPos[i].x = 0;
                    currentLevel.dynThornsSpeed[i].x = (short) Math.abs(spdX);
                } else if (currentLevel.dynThornsPos[i].x >= maxX) {
                    currentLevel.dynThornsPos[i].x = (short) maxX;
                    currentLevel.dynThornsSpeed[i].x = (short) -Math.abs(spdX);
                }
            }

            if (maxY > 0 && spdY != 0) {
                currentLevel.dynThornsPos[i].y = (short) (currentLevel.dynThornsPos[i].y + spdY);
                if (currentLevel.dynThornsPos[i].y <= 0) {
                    currentLevel.dynThornsPos[i].y = 0;
                    currentLevel.dynThornsSpeed[i].y = (short) Math.abs(spdY);
                } else if (currentLevel.dynThornsPos[i].y >= maxY) {
                    currentLevel.dynThornsPos[i].y = (short) maxY;
                    currentLevel.dynThornsSpeed[i].y = (short) -Math.abs(spdY);
                }
            }
        }
    }

    private void drawVirtual() {
        if (currentLevel != null && renderer != null) {
            int hoopsLeft = currentLevel.hoopsTotal - hoopsScored;
            int boost = (ball != null) ? Math.max(ball.powerUpSpeed, Math.max(ball.powerUpGravity, ball.powerUpJump)) : 0;
            renderer.render(virtualCanvas, currentLevel, ball, camX, camY, lives, Math.max(0, hoopsLeft), score, exitOffset, boost);
        }
    }

    private void drawToScreen() {
        if (!holder.getSurface().isValid()) return;
        Canvas c = holder.lockCanvas();
        if (c == null) return;

        if (gameState == STATE_SPLASH_NOKIA) {
            c.drawColor(Color.BLACK);
            drawSplashScaled(c, splashNokia, 0.60f);
        } else if (gameState == STATE_SPLASH_BOUNCE) {
            c.drawColor(0xFF6CFFFB);
            drawSplashScaled(c, splashBounce, 0.80f);
        } else if (gameState == STATE_PLAYING || gameState == STATE_PAUSED) {
            c.drawColor(0xFF041938);
            c.drawBitmap(virtualBitmap, scaleMatrix, screenPaint);
            drawInGameControls(c);
            if (gameState == STATE_PAUSED) {
                drawDialogOverlay(c, "PAUSED", "RESUME", "MAIN MENU", true);
            }
        } else if (gameState == STATE_MENU) {
            c.drawColor(0xFF041938);
            drawMainMenu(c);
        } else if (gameState == STATE_LEVEL_SELECT) {
            c.drawColor(0xFF041938);
            drawLevelSelect(c);
        } else if (gameState == STATE_SETTINGS) {
            c.drawColor(0xFF041938);
            drawSettingsScreen(c);
        } else if (gameState == STATE_LEVEL_COMPLETE) {
            c.drawColor(0xFF041938);
            c.drawBitmap(virtualBitmap, scaleMatrix, screenPaint);
            drawDialogOverlay(c, "LEVEL COMPLETED!", "NEXT LEVEL", "MAIN MENU", false);
        } else if (gameState == STATE_GAME_OVER) {
            c.drawColor(0xFF041938);
            c.drawBitmap(virtualBitmap, scaleMatrix, screenPaint);
            drawDialogOverlay(c, "GAME OVER", "TRY AGAIN", "MAIN MENU", false);
        } else if (gameState == STATE_HIGH_SCORE) {
            c.drawColor(0xFF041938);
            drawHighScoreScreen(c);
        }

        holder.unlockCanvasAndPost(c);
    }

    private void drawSplashScaled(Canvas c, Bitmap splash, float scaleFactor) {
        if (splash != null) {
            float baseScale = Math.min((float) getWidth() / splash.getWidth(), (float) getHeight() / splash.getHeight());
            float finalScale = baseScale * scaleFactor;
            int targetW = (int) (splash.getWidth() * finalScale);
            int targetH = (int) (splash.getHeight() * finalScale);
            int x = (getWidth() - targetW) / 2;
            int y = (getHeight() - targetH) / 2;
            RectF dest = new RectF(x, y, x + targetW, y + targetH);
            c.drawBitmap(splash, null, dest, screenPaint);
        }
    }

    private void drawInGameControls(Canvas c) {
        if (controlType == CONTROL_SIMPLE) {
            uiPaint.setColor(0x44FFFFFF);
            c.drawRoundRect(btnLeft, 24, 24, uiPaint);
            c.drawRoundRect(btnRight, 24, 24, uiPaint);
            c.drawRoundRect(btnJump, 24, 24, uiPaint);

            uiPaint.setColor(Color.WHITE);
            uiPaint.setTextSize(Math.min(btnLeft.height() * 0.45f, 44));
            uiPaint.setTextAlign(Paint.Align.CENTER);
            c.drawText("◀", btnLeft.centerX(), btnLeft.centerY() + 15, uiPaint);
            c.drawText("▶", btnRight.centerX(), btnRight.centerY() + 15, uiPaint);
            c.drawText("▲", btnJump.centerX(), btnJump.centerY() + 15, uiPaint);
        } else if (controlType == CONTROL_NUMPAD) {
            for (int i = 0; i < 12; i++) {
                uiPaint.setColor(0x33FFFFFF);
                c.drawRoundRect(btnNumpad[i], 16, 16, uiPaint);
                uiPaint.setColor(Color.WHITE);
                uiPaint.setTextSize(Math.min(btnNumpad[i].height() * 0.40f, 32));
                uiPaint.setTextAlign(Paint.Align.CENTER);
                c.drawText(numpadLabels[i], btnNumpad[i].centerX(), btnNumpad[i].centerY() + 11, uiPaint);
            }
        }
        // Jika CONTROL_KEYBOARD: Layar dibiarkan bersih tanpa tombol sentuh
    }

    private void drawRetroBevelButton(Canvas c, RectF r, String text, int baseColor, boolean isSelected) {
        uiPaint.setColor(isSelected ? 0xFF0D67D4 : baseColor);
        c.drawRect(r, uiPaint);

        borderPaint.setColor(isSelected ? 0xFFFFFFFF : 0x88FFFFFF);
        borderPaint.setStrokeWidth(isSelected ? 5.0f : 3.0f);
        c.drawLine(r.left, r.top, r.right, r.top, borderPaint);
        c.drawLine(r.left, r.top, r.left, r.bottom, borderPaint);

        borderPaint.setColor(isSelected ? 0xFF002244 : 0xFF000000);
        c.drawLine(r.left, r.bottom, r.right, r.bottom, borderPaint);
        c.drawLine(r.right, r.top, r.right, r.bottom, borderPaint);
        borderPaint.setStrokeWidth(3.0f);

        uiPaint.setColor(Color.WHITE);
        uiPaint.setTextSize(Math.min(r.height() * 0.48f, 32));
        uiPaint.setTextAlign(Paint.Align.CENTER);
        c.drawText(text, r.centerX(), r.centerY() + r.height() * 0.16f, uiPaint);
    }

    private void drawRetroBevelButton(Canvas c, RectF r, String text, int baseColor) {
        drawRetroBevelButton(c, r, text, baseColor, false);
    }

    private void drawMainMenu(Canvas c) {
        uiPaint.setColor(Color.WHITE);
        uiPaint.setTextAlign(Paint.Align.CENTER);
        uiPaint.setTextSize(getWidth() > getHeight() ? 52 : 64);
        c.drawText("BOUNCE", getWidth() / 2f, getHeight() * (getWidth() > getHeight() ? 0.18f : 0.16f), uiPaint);

        drawRetroBevelButton(c, btnMenuContinue, "CONTINUE", hasActiveGame ? 0xFF0853AA : 0xFF2A3644, menuCursorIndex == 0);
        drawRetroBevelButton(c, btnMenuNewGame, "NEW GAME", 0xFF0853AA, menuCursorIndex == 1);
        drawRetroBevelButton(c, btnMenuLevelSelect, "LEVEL SELECT", 0xFF0853AA, menuCursorIndex == 2);
        drawRetroBevelButton(c, btnMenuHighScore, "HIGH SCORE", 0xFF0853AA, menuCursorIndex == 3);
        drawRetroBevelButton(c, btnMenuSettings, "SETTINGS", 0xFF0853AA, menuCursorIndex == 4);
    }

    private void drawSettingsScreen(Canvas c) {
        boolean isLandscape = getWidth() > getHeight();
        float btnW = isLandscape ? Math.min(getWidth() * 0.44f, 460f) : Math.min(getWidth() * 0.82f, 520f);
        float btnH = Math.min(getHeight() * 0.105f, isLandscape ? 44f : 56f);
        float gap = isLandscape ? 8f : 12f;
        float bottomGap = gap * 2.2f;
        float startX = (getWidth() - btnW) / 2f;

        float totalH = 5 * btnH + 3 * gap + bottomGap;
        float buttonsTop = getHeight() * 0.50f - (totalH / 2f) + (isLandscape ? 12f : 24f);

        uiPaint.setColor(Color.WHITE);
        uiPaint.setTextAlign(Paint.Align.CENTER);
        uiPaint.setTextSize(isLandscape ? 42 : 48);
        c.drawText("SETTINGS", getWidth() / 2f, buttonsTop - (isLandscape ? 52f : 68f), uiPaint);

        uiPaint.setTextSize(isLandscape ? 22 : 26);
        c.drawText("SELECT CONTROLLER:", getWidth() / 2f, buttonsTop - (isLandscape ? 16f : 22f), uiPaint);

        btnSettingSimple.set(startX, buttonsTop, startX + btnW, buttonsTop + btnH);
        float numpadTop = buttonsTop + btnH + gap;
        btnSettingNumpad.set(startX, numpadTop, startX + btnW, numpadTop + btnH);
        float kbTop = numpadTop + btnH + gap;
        btnSettingKeyboard.set(startX, kbTop, startX + btnW, kbTop + btnH);
        float fsTop = kbTop + btnH + gap;
        btnSettingFullscreen.set(startX, fsTop, startX + btnW, fsTop + btnH);
        float backTop = fsTop + btnH + bottomGap;
        btnBack.set(startX, backTop, startX + btnW, backTop + btnH);

        int simpleColor = (controlType == CONTROL_SIMPLE) ? 0xFF1474E0 : 0xFF2A3644;
        int numpadColor = (controlType == CONTROL_NUMPAD) ? 0xFF1474E0 : 0xFF2A3644;
        int kbColor = (controlType == CONTROL_KEYBOARD) ? 0xFF1474E0 : 0xFF2A3644;
        int fsColor = isFullscreen ? 0xFF1474E0 : 0xFF2A3644;

        drawRetroBevelButton(c, btnSettingSimple, "SIMPLE (3 BUTTONS)", simpleColor, menuCursorIndex == 0);
        drawRetroBevelButton(c, btnSettingNumpad, "NUMERIC KEYPAD", numpadColor, menuCursorIndex == 1);
        drawRetroBevelButton(c, btnSettingKeyboard, "PHYSICAL KEYPAD", kbColor, menuCursorIndex == 2);
        drawRetroBevelButton(c, btnSettingFullscreen, isFullscreen ? "FULL SCREEN: ON" : "FULL SCREEN: OFF", fsColor, menuCursorIndex == 3);
        drawRetroBevelButton(c, btnBack, "BACK", 0xFF445566, menuCursorIndex == 4);
    }

    private void drawDialogOverlay(Canvas c, String title, String btn1Text, String btn2Text, boolean hasSettings) {
        boolean isLandscape = getWidth() > getHeight();
        c.drawColor(0xCC000000);

        float btnW = isLandscape ? Math.min(getWidth() * 0.45f, 450f) : Math.min(getWidth() * 0.85f, 500f);
        float btnH = isLandscape ? Math.min(getHeight() * 0.12f, 60f) : 65f;
        float gap = isLandscape ? 15f : 17f;
        float bottomGap = gap * 2.2f;
        float startX = (getWidth() - btnW) / 2f;

        float totalH = hasSettings ? (3 * btnH + gap + bottomGap) : (2 * btnH + bottomGap);
        float buttonsTop = getHeight() * 0.50f - (totalH / 2f) + (hasSettings ? 24f : 14f);

        uiPaint.setColor(Color.WHITE);
        uiPaint.setTextAlign(Paint.Align.CENTER);
        uiPaint.setTextSize(isLandscape ? 48 : 52);
        c.drawText(title, getWidth() / 2f, buttonsTop - (isLandscape ? 72f : 88f), uiPaint);

        uiPaint.setTextSize(isLandscape ? 28 : 30);
        c.drawText("Score: " + String.format("%07d", score), getWidth() / 2f, buttonsTop - (isLandscape ? 25f : 32f), uiPaint);

        btnDialogPrimary.set(startX, buttonsTop, startX + btnW, buttonsTop + btnH);
        drawRetroBevelButton(c, btnDialogPrimary, btn1Text, 0xFF0853AA, menuCursorIndex == 0);

        if (hasSettings) {
            float settingsTop = buttonsTop + btnH + gap;
            btnDialogSettings.set(startX, settingsTop, startX + btnW, settingsTop + btnH);
            drawRetroBevelButton(c, btnDialogSettings, "SETTINGS", 0xFF0853AA, menuCursorIndex == 1);

            float secondaryTop = settingsTop + btnH + bottomGap;
            btnDialogSecondary.set(startX, secondaryTop, startX + btnW, secondaryTop + btnH);
            drawRetroBevelButton(c, btnDialogSecondary, btn2Text, 0xFF662222, menuCursorIndex == 2);
        } else {
            float secondaryTop = buttonsTop + btnH + bottomGap;
            btnDialogSecondary.set(startX, secondaryTop, startX + btnW, secondaryTop + btnH);
            drawRetroBevelButton(c, btnDialogSecondary, btn2Text, 0xFF662222, menuCursorIndex == 1);
        }
    }

    public boolean handleKeyDown(int keyCode, KeyEvent event) {
        if (gameState == STATE_SPLASH_NOKIA || gameState == STATE_SPLASH_BOUNCE) {
            gameState = STATE_MENU;
            menuCursorIndex = hasActiveGame ? 0 : 1;
            return true;
        }

        if (gameState == STATE_PLAYING) {
            boolean keyChanged = false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_A) {
                keyLeftHeld = true;
                keyChanged = true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_D) {
                keyRightHeld = true;
                keyChanged = true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_5
                    || keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_W) {
                keyJumpHeld = true;
                keyChanged = true;
            }

            if (keyChanged) {
                if (ball != null) {
                    ball.resetAllInputs();
                    if (keyLeftHeld) ball.setInput(1);
                    if (keyRightHeld) ball.setInput(2);
                    if (keyJumpHeld) ball.setInput(8);
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_POUND || (keyCode == KeyEvent.KEYCODE_3 && event.isShiftPressed()) || keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT || keyCode == KeyEvent.KEYCODE_G) { handleKeypadPress("#"); return true; } if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9 && !event.isShiftPressed()) {
                handleKeypadPress(String.valueOf(keyCode - KeyEvent.KEYCODE_0));
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_STAR) {
                handleKeypadPress("*");
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_POUND) {
                handleKeypadPress("#");
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_P || keyCode == KeyEvent.KEYCODE_MENU) {
                gameState = STATE_PAUSED;
                menuCursorIndex = 0;
                keyLeftHeld = keyRightHeld = keyJumpHeld = false;
                if (ball != null) ball.resetAllInputs();
                return true;
            }
        }

        boolean isUp = (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_W || keyCode == KeyEvent.KEYCODE_2);
        boolean isDown = (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_S || keyCode == KeyEvent.KEYCODE_8);
        boolean isEnter = (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
                || keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_5);

        if (gameState == STATE_MENU) {
            int minIdx = hasActiveGame ? 0 : 1;
            if (isUp) {
                menuCursorIndex--;
                if (menuCursorIndex < minIdx) menuCursorIndex = 4;
                return true;
            }
            if (isDown) {
                menuCursorIndex++;
                if (menuCursorIndex > 4) menuCursorIndex = minIdx;
                return true;
            }
            if (isEnter) {
                if (menuCursorIndex == 0 && hasActiveGame) gameState = STATE_PLAYING;
                else if (menuCursorIndex == 1) startNewGame(1);
                else if (menuCursorIndex == 2) { gameState = STATE_LEVEL_SELECT; menuCursorIndex = 0; }
                else if (menuCursorIndex == 3) gameState = STATE_HIGH_SCORE;
                else if (menuCursorIndex == 4) { settingsReturnState = STATE_MENU; gameState = STATE_SETTINGS; menuCursorIndex = 0; }
                return true;
            }
        } else if (gameState == STATE_SETTINGS) {
            if (isUp) {
                menuCursorIndex = (menuCursorIndex <= 0) ? 4 : menuCursorIndex - 1;
                return true;
            }
            if (isDown) {
                menuCursorIndex = (menuCursorIndex >= 4) ? 0 : menuCursorIndex + 1;
                return true;
            }
            if (isEnter) {
                if (menuCursorIndex == 0) { controlType = CONTROL_SIMPLE; saveGameProgress(); updateLayout(getWidth(), getHeight()); }
                else if (menuCursorIndex == 1) { controlType = CONTROL_NUMPAD; saveGameProgress(); updateLayout(getWidth(), getHeight()); }
                else if (menuCursorIndex == 2) { controlType = CONTROL_KEYBOARD; saveGameProgress(); updateLayout(getWidth(), getHeight()); }
                else if (menuCursorIndex == 3) {
                    isFullscreen = !isFullscreen;
                    saveGameProgress();
                    if (getContext() instanceof MainActivity) {
                        ((MainActivity) getContext()).applyFullscreenMode(isFullscreen);
                    }
                } else if (menuCursorIndex == 4) {
                    gameState = settingsReturnState;
                    menuCursorIndex = (gameState == STATE_PAUSED) ? 1 : 4;
                }
                return true;
            }
        } else if (gameState == STATE_PAUSED) {
            if (isUp) {
                menuCursorIndex = (menuCursorIndex <= 0) ? 2 : menuCursorIndex - 1;
                return true;
            }
            if (isDown) {
                menuCursorIndex = (menuCursorIndex >= 2) ? 0 : menuCursorIndex + 1;
                return true;
            }
            if (isEnter) {
                if (menuCursorIndex == 0) gameState = STATE_PLAYING;
                else if (menuCursorIndex == 1) { settingsReturnState = STATE_PAUSED; gameState = STATE_SETTINGS; menuCursorIndex = 0; }
                else if (menuCursorIndex == 2) { gameState = STATE_MENU; menuCursorIndex = hasActiveGame ? 0 : 1; }
                return true;
            }
        } else if (gameState == STATE_LEVEL_COMPLETE || gameState == STATE_GAME_OVER) {
            if (isUp || isDown) {
                menuCursorIndex = (menuCursorIndex == 0) ? 1 : 0;
                return true;
            }
            if (isEnter) {
                if (gameState == STATE_LEVEL_COMPLETE) {
                    if (menuCursorIndex == 0) {
                        if (currentLevelId < 11) { loadLevel(currentLevelId + 1); gameState = STATE_PLAYING; }
                        else { gameState = STATE_MENU; menuCursorIndex = 1; }
                    } else { gameState = STATE_MENU; menuCursorIndex = 1; }
                } else {
                    if (menuCursorIndex == 0) startNewGame(currentLevelId);
                    else { gameState = STATE_MENU; menuCursorIndex = 1; }
                }
                return true;
            }
        } else if (gameState == STATE_LEVEL_SELECT) {
            boolean isLeft = (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_A || keyCode == KeyEvent.KEYCODE_4);
            boolean isRight = (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_D || keyCode == KeyEvent.KEYCODE_6);
            int cols = (getWidth() > getHeight()) ? 6 : 4;

            if (isLeft) {
                if (menuCursorIndex == 11) menuCursorIndex = maxUnlockedLevel - 1;
                else if (menuCursorIndex > 0) menuCursorIndex--;
                else menuCursorIndex = 11;
                return true;
            }
            if (isRight) {
                if (menuCursorIndex == 11) menuCursorIndex = 0;
                else if (menuCursorIndex < maxUnlockedLevel - 1) menuCursorIndex++;
                else menuCursorIndex = 11;
                return true;
            }
            if (isUp) {
                if (menuCursorIndex == 11) menuCursorIndex = maxUnlockedLevel - 1;
                else if (menuCursorIndex - cols >= 0) menuCursorIndex -= cols;
                else menuCursorIndex = 11;
                return true;
            }
            if (isDown) {
                if (menuCursorIndex == 11) menuCursorIndex = 0;
                else if (menuCursorIndex + cols < maxUnlockedLevel) menuCursorIndex += cols;
                else menuCursorIndex = 11;
                return true;
            }
            if (isEnter) {
                if (menuCursorIndex == 11) {
                    gameState = STATE_MENU;
                    menuCursorIndex = 2;
                } else if (menuCursorIndex >= 0 && menuCursorIndex < maxUnlockedLevel) {
                    startNewGame(menuCursorIndex + 1);
                }
                return true;
            }
        } else if (gameState == STATE_HIGH_SCORE) {
            if (isEnter) {
                gameState = STATE_MENU;
                menuCursorIndex = 3;
                return true;
            }
        }
        return false;
    }

    // FIX TOMBOL LENGKET: Lepas tombol fisik langsung mereset input bola
    public boolean handleKeyUp(int keyCode, KeyEvent event) {
        if (true) {
            boolean keyChanged = false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_A) {
                keyLeftHeld = false;
                keyChanged = true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_D) {
                keyRightHeld = false;
                keyChanged = true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_5
                    || keyCode == KeyEvent.KEYCODE_SPACE || keyCode == KeyEvent.KEYCODE_W) {
                keyJumpHeld = false;
                keyChanged = true;
            }
            if (keyChanged) {
                if (ball != null) {
                    ball.resetAllInputs();
                    if (keyLeftHeld) ball.setInput(1);
                    if (keyRightHeld) ball.setInput(2);
                    if (keyJumpHeld) ball.setInput(8);
                }
                return true;
            }
        }
        return false;
    }

    private void drawLevelSelect(Canvas c) {
        boolean isLandscape = getWidth() > getHeight();
        uiPaint.setColor(Color.WHITE);
        uiPaint.setTextAlign(Paint.Align.CENTER);
        uiPaint.setTextSize(isLandscape ? 42 : 46);

        float lStartY = getHeight() * (isLandscape ? 0.22f : 0.20f);
        c.drawText("SELECT LEVEL", getWidth() / 2f, lStartY - 30f, uiPaint);

        for (int i = 0; i < 11; i++) {
            boolean unlocked = (i + 1) <= maxUnlockedLevel;
            drawRetroBevelButton(c, btnLevels[i], String.valueOf(i + 1), unlocked ? 0xFF0853AA : 0xFF2A3644, menuCursorIndex == i);
        }
        drawRetroBevelButton(c, btnBack, "BACK", 0xFF445566, menuCursorIndex == 11);
    }

    private void drawHighScoreScreen(Canvas c) {
        boolean isLandscape = getWidth() > getHeight();
        float btnW = isLandscape ? Math.min(getWidth() * 0.45f, 450f) : Math.min(getWidth() * 0.85f, 500f);
        float btnH = isLandscape ? Math.min(getHeight() * 0.12f, 60f) : 65f;
        float startX = (getWidth() - btnW) / 2f;

        float titleY = getHeight() * 0.38f;
        float scoreY = titleY + 68f;
        btnBack.set(startX, scoreY + 50f, startX + btnW, scoreY + 50f + btnH);

        uiPaint.setColor(Color.WHITE);
        uiPaint.setTextAlign(Paint.Align.CENTER);
        uiPaint.setTextSize(isLandscape ? 44 : 48);
        c.drawText("HIGH SCORE", getWidth() / 2f, titleY, uiPaint);

        uiPaint.setTextSize(isLandscape ? 60 : 66);
        uiPaint.setColor(0xFFFFD700);
        c.drawText(String.format("%07d", highScore), getWidth() / 2f, scoreY, uiPaint);

        drawRetroBevelButton(c, btnBack, "BACK", 0xFF445566, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float tx = event.getX();
        float ty = event.getY();

        if (action == MotionEvent.ACTION_DOWN) {
            if (gameState == STATE_SPLASH_NOKIA || gameState == STATE_SPLASH_BOUNCE) {
                gameState = STATE_MENU;
                menuCursorIndex = hasActiveGame ? 0 : 1;
                return true;
            }
            if (gameState == STATE_MENU) {
                if (hasActiveGame && btnMenuContinue.contains(tx, ty)) { menuCursorIndex = 0; gameState = STATE_PLAYING; }
                else if (btnMenuNewGame.contains(tx, ty)) { menuCursorIndex = 1; startNewGame(1); }
                else if (btnMenuLevelSelect.contains(tx, ty)) { menuCursorIndex = 0; gameState = STATE_LEVEL_SELECT; }
                else if (btnMenuHighScore.contains(tx, ty)) { menuCursorIndex = 3; gameState = STATE_HIGH_SCORE; }
                else if (btnMenuSettings.contains(tx, ty)) {
                    menuCursorIndex = 0;
                    settingsReturnState = STATE_MENU;
                    gameState = STATE_SETTINGS;
                }
                return true;
            } else if (gameState == STATE_SETTINGS) {
                if (btnSettingSimple.contains(tx, ty)) {
                    menuCursorIndex = 0;
                    controlType = CONTROL_SIMPLE;
                    saveGameProgress();
                    updateLayout(getWidth(), getHeight());
                } else if (btnSettingNumpad.contains(tx, ty)) {
                    menuCursorIndex = 1;
                    controlType = CONTROL_NUMPAD;
                    saveGameProgress();
                    updateLayout(getWidth(), getHeight());
                } else if (btnSettingKeyboard.contains(tx, ty)) {
                    menuCursorIndex = 2;
                    controlType = CONTROL_KEYBOARD;
                    saveGameProgress();
                    updateLayout(getWidth(), getHeight());
                } else if (btnSettingFullscreen.contains(tx, ty)) {
                    menuCursorIndex = 3;
                    isFullscreen = !isFullscreen;
                    saveGameProgress();
                    if (getContext() instanceof MainActivity) {
                        ((MainActivity) getContext()).applyFullscreenMode(isFullscreen);
                    }
                } else if (btnBack.contains(tx, ty)) {
                    gameState = settingsReturnState;
                    menuCursorIndex = (gameState == STATE_PAUSED) ? 1 : 4;
                }
                return true;
            } else if (gameState == STATE_LEVEL_SELECT) {
                for (int i = 0; i < 11; i++) {
                    if ((i + 1) <= maxUnlockedLevel && btnLevels[i].contains(tx, ty)) {
                        menuCursorIndex = i;
                        startNewGame(i + 1);
                        return true;
                    }
                }
                if (btnBack.contains(tx, ty)) { gameState = STATE_MENU; menuCursorIndex = 2; }
                return true;
            } else if (gameState == STATE_HIGH_SCORE) {
                if (btnBack.contains(tx, ty)) { gameState = STATE_MENU; menuCursorIndex = 3; }
                return true;
            } else if (gameState == STATE_PAUSED) {
                if (btnDialogPrimary.contains(tx, ty)) { menuCursorIndex = 0; gameState = STATE_PLAYING; }
                else if (btnDialogSettings.contains(tx, ty)) {
                    menuCursorIndex = 0;
                    settingsReturnState = STATE_PAUSED;
                    gameState = STATE_SETTINGS;
                } else if (btnDialogSecondary.contains(tx, ty)) { gameState = STATE_MENU; menuCursorIndex = hasActiveGame ? 0 : 1; }
                return true;
            } else if (gameState == STATE_LEVEL_COMPLETE) {
                if (btnDialogPrimary.contains(tx, ty)) {
                    if (currentLevelId < 11) { loadLevel(currentLevelId + 1); gameState = STATE_PLAYING; }
                    else { gameState = STATE_MENU; menuCursorIndex = 1; }
                } else if (btnDialogSecondary.contains(tx, ty)) { gameState = STATE_MENU; menuCursorIndex = 1; }
                return true;
            } else if (gameState == STATE_GAME_OVER) {
                if (btnDialogPrimary.contains(tx, ty)) startNewGame(currentLevelId);
                else if (btnDialogSecondary.contains(tx, ty)) { gameState = STATE_MENU; menuCursorIndex = 1; }
                return true;
            }
        }

        if (gameState == STATE_PLAYING && ball != null) {
            if (controlType == CONTROL_KEYBOARD) {
                return true;
            }
            int pointerCount = event.getPointerCount();
            ball.resetAllInputs();

            if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
                if (controlType == CONTROL_SIMPLE) {
                    for (int i = 0; i < pointerCount; i++) {
                        float px = event.getX(i);
                        float py = event.getY(i);
                        if (btnLeft.contains(px, py)) ball.setInput(1);
                        if (btnRight.contains(px, py)) ball.setInput(2);
                        if (btnJump.contains(px, py)) ball.setInput(8);
                    }
                } else {
                    for (int i = 0; i < pointerCount; i++) {
                        float px = event.getX(i);
                        float py = event.getY(i);
                        if (btnNumpad[3].contains(px, py)) ball.setInput(1);
                        if (btnNumpad[5].contains(px, py)) ball.setInput(2);
                        if (btnNumpad[1].contains(px, py) || btnNumpad[4].contains(px, py)) ball.setInput(8);
                    }
                }
            }

            if (keyLeftHeld) ball.setInput(1);
            if (keyRightHeld) ball.setInput(2);
            if (keyJumpHeld) ball.setInput(8);

            if (controlType == CONTROL_NUMPAD && (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)) {
                int actionIdx = event.getActionIndex();
                for (int i = 0; i < 12; i++) {
                    if (btnNumpad[i].contains(event.getX(actionIdx), event.getY(actionIdx))) {
                        handleKeypadPress(numpadLabels[i]);
                        break;
                    }
                }
            }
        }
        return true;
    }

    public boolean handleBackPressed() {
        keyLeftHeld = keyRightHeld = keyJumpHeld = false;
        if (gameState == STATE_SPLASH_NOKIA || gameState == STATE_SPLASH_BOUNCE) {
            gameState = STATE_MENU;
            menuCursorIndex = hasActiveGame ? 0 : 1;
            return true;
        } else if (gameState == STATE_PLAYING) {
            gameState = STATE_PAUSED;
            menuCursorIndex = 0;
            if (ball != null) ball.resetAllInputs();
            return true;
        } else if (gameState == STATE_SETTINGS) {
            gameState = settingsReturnState;
            menuCursorIndex = (gameState == STATE_PAUSED) ? 1 : 4;
            return true;
        } else if (gameState == STATE_LEVEL_SELECT || gameState == STATE_HIGH_SCORE || gameState == STATE_PAUSED) {
            gameState = STATE_MENU;
            menuCursorIndex = hasActiveGame ? 0 : 1;
            return true;
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder h) {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder h, int format, int width, int height) {
        updateLayout(width, height);
    }

    // Canvas Otomatis Berada di Titik Tengah Layar Saat Mode Keyboard Aktif
    public void updateLayout(int width, int height) {
        if (width <= 0 || height <= 0) return;
        boolean isLandscape = width > height;

        if (isLandscape) {
            float scale = (float) height / 128f;
            float canvasScreenSize = 128f * scale;
            float canvasLeft = (width - canvasScreenSize) / 2f;
            scaleMatrix.reset();
            scaleMatrix.postScale(scale, scale);
            scaleMatrix.postTranslate(canvasLeft, 0f);

            float leftW = canvasLeft;
            float rightX = canvasLeft + canvasScreenSize;

            float sBtnGap = Math.max(leftW * 0.05f, 10f);
            float sBtnW = Math.min((leftW - 3 * sBtnGap) / 2f, height * 0.22f);
            float sBtnY = height * 0.65f - sBtnW / 2f;
            float sLeftX = (leftW - (2 * sBtnW + sBtnGap)) / 2f;
            btnLeft.set(sLeftX, sBtnY, sLeftX + sBtnW, sBtnY + sBtnW);
            btnRight.set(sLeftX + sBtnW + sBtnGap, sBtnY, sLeftX + 2 * sBtnW + sBtnGap, sBtnY + sBtnW);

            float jBtnSize = Math.min(leftW * 0.55f, height * 0.28f);
            btnJump.set(rightX + (leftW - jBtnSize) / 2f, height * 0.65f - jBtnSize / 2f, rightX + (leftW - jBtnSize) / 2f + jBtnSize, height * 0.65f + jBtnSize / 2f);

            float marginX = Math.max(leftW * 0.05f, 8f);
            float gapX = Math.max(leftW * 0.03f, 6f);
            float gapY = Math.max(height * 0.02f, 8f);
            float keyW = (leftW - 2 * marginX - 2 * gapX) / 3f;
            float keyH = Math.min(height * 0.16f, keyW * 0.85f);
            float gridY = height * 0.65f - (2 * keyH + gapY) / 2f;

            int[][] leftGrid = {{3, 4, 5}, {6, 7, 8}};
            for (int r = 0; r < 2; r++) {
                for (int col = 0; col < 3; col++) {
                    btnNumpad[leftGrid[r][col]].set(marginX + col * (keyW + gapX), gridY + r * (keyH + gapY), marginX + col * (keyW + gapX) + keyW, gridY + r * (keyH + gapY) + keyH);
                }
            }

            int[][] rightGrid = {{0, 1, 2}, {9, 10, 11}};
            for (int r = 0; r < 2; r++) {
                for (int col = 0; col < 3; col++) {
                    btnNumpad[rightGrid[r][col]].set(rightX + marginX + col * (keyW + gapX), gridY + r * (keyH + gapY), rightX + marginX + col * (keyW + gapX) + keyW, gridY + r * (keyH + gapY) + keyH);
                }
            }

            float mBtnW = Math.min(width * 0.38f, 380f);
            float mBtnH = Math.min(height * 0.10f, 50f);
            float mSpacing = mBtnH + 12f;
            float mStartX = (width - mBtnW) / 2f;
            float mStartY = height * 0.26f;

            btnMenuContinue.set(mStartX, mStartY, mStartX + mBtnW, mStartY + mBtnH);
            btnMenuNewGame.set(mStartX, mStartY + mSpacing, mStartX + mBtnW, mStartY + mSpacing + mBtnH);
            btnMenuLevelSelect.set(mStartX, mStartY + mSpacing * 2, mStartX + mBtnW, mStartY + mSpacing * 2 + mBtnH);
            btnMenuHighScore.set(mStartX, mStartY + mSpacing * 3, mStartX + mBtnW, mStartY + mSpacing * 3 + mBtnH);
            btnMenuSettings.set(mStartX, mStartY + mSpacing * 4, mStartX + mBtnW, mStartY + mSpacing * 4 + mBtnH);

            float lGridW = Math.min(width * 0.72f, 720f);
            float lStartX = (width - lGridW) / 2f;
            float lCellW = (lGridW - 5 * 12f) / 6f;
            float lCellH = Math.min(height * 0.12f, 52f);
            float lStartY = height * 0.24f;

            for (int i = 0; i < 11; i++) {
                btnLevels[i].set(lStartX + (i % 6) * (lCellW + 12f), lStartY + (i / 6) * (lCellH + 12f), lStartX + (i % 6) * (lCellW + 12f) + lCellW, lStartY + (i / 6) * (lCellH + 12f) + lCellH);
            }
            btnBack.set(mStartX, lStartY + 2 * (lCellH + 12f) + 16f, mStartX + mBtnW, lStartY + 2 * (lCellH + 12f) + 16f + mBtnH);

        } else {
            float scale = (float) width / 128f;
            float gameScreenHeight = 128f * scale;
            float canvasTop = (controlType == CONTROL_KEYBOARD) ? (height - gameScreenHeight) / 2f : 0f;

            scaleMatrix.reset();
            scaleMatrix.postScale(scale, scale);
            scaleMatrix.postTranslate(0f, canvasTop);

            float btnSize = width * 0.22f;
            float btnY = gameScreenHeight + (height - gameScreenHeight - btnSize) / 2f;

            btnLeft.set(width * 0.08f, btnY, width * 0.08f + btnSize, btnY + btnSize);
            btnRight.set(width * 0.35f, btnY, width * 0.35f + btnSize, btnY + btnSize);
            btnJump.set(width * 0.70f, btnY, width * 0.70f + btnSize, btnY + btnSize);

            float numPadAreaTop = gameScreenHeight + 15;
            float numKeyW = (width - 60) / 3f;
            float numKeyH = (height - numPadAreaTop - 25) / 4.2f;

            for (int i = 0; i < 12; i++) {
                btnNumpad[i].set(15 + (i % 3) * (numKeyW + 15), numPadAreaTop + (i / 3) * (numKeyH + 12), 15 + (i % 3) * (numKeyW + 15) + numKeyW, numPadAreaTop + (i / 3) * (numKeyH + 12) + numKeyH);
            }

            float mBtnW = width * 0.74f;
            float mBtnH = 58;
            float mStartX = (width - mBtnW) / 2f;
            float mStartY = height * 0.28f;
            float mSpacing = 72;

            btnMenuContinue.set(mStartX, mStartY, mStartX + mBtnW, mStartY + mBtnH);
            btnMenuNewGame.set(mStartX, mStartY + mSpacing, mStartX + mBtnW, mStartY + mSpacing + mBtnH);
            btnMenuLevelSelect.set(mStartX, mStartY + mSpacing * 2, mStartX + mBtnW, mStartY + mSpacing * 2 + mBtnH);
            btnMenuHighScore.set(mStartX, mStartY + mSpacing * 3, mStartX + mBtnW, mStartY + mSpacing * 3 + mBtnH);
            btnMenuSettings.set(mStartX, mStartY + mSpacing * 4, mStartX + mBtnW, mStartY + mSpacing * 4 + mBtnH);

            float cellW = (width - width * 0.24f) / 4f;
            float cellH = 60;
            float gridStartY = height * 0.22f;

            for (int i = 0; i < 11; i++) {
                btnLevels[i].set(width * 0.12f + (i % 4) * cellW + 8, gridStartY + (i / 4) * (cellH + 14), width * 0.12f + (i % 4) * cellW + cellW - 8, gridStartY + (i / 4) * (cellH + 14) + cellH);
            }
            btnBack.set(mStartX, gridStartY + 3 * (cellH + 14) + 18f, mStartX + mBtnW, gridStartY + 3 * (cellH + 14) + 72f);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder h) {
        isRunning = false;
        try {
            if (gameThread != null) gameThread.join();
        } catch (InterruptedException ignored) {}
        if (soundManager != null) soundManager.release();
    }

    @Override public int getMapWidth() { return (currentLevel != null) ? currentLevel.width : 0; }
    @Override public int getMapHeight() { return (currentLevel != null) ? currentLevel.height : 0; }
    @Override public short[][] getLevelTiles() { return (currentLevel != null) ? currentLevel.tiles : new short[0][0]; }
    @Override public boolean isGodMode() { return godMode; }
    @Override public void decreaseLife() { if (!godMode) lives--; }
    @Override public void addLife() { if (lives < 5) lives++; }
    @Override public void addScore(int points) { score += points; }
    @Override public void hoopScored() { hoopsScored++; if (currentLevel != null && hoopsScored >= currentLevel.hoopsTotal) exitUnlocked = true; }
    @Override public void triggerGameLevelComplete() { if (gameState != STATE_PLAYING) return; menuCursorIndex = 0; saveGameProgress(); keyLeftHeld = false; keyRightHeld = false; keyJumpHeld = false; if (ball != null) ball.resetAllInputs(); gameState = STATE_LEVEL_COMPLETE; }
    @Override public int testPointInsideDynThorns(int tileX, int tileY) {
        if (currentLevel != null && currentLevel.dynThornsCount > 0) {
            for (int i = 0; i < currentLevel.dynThornsCount; i++) {
                if (tileX >= currentLevel.dynThornsBottomLeft[i].x && tileX < currentLevel.dynThornsTopRight[i].x
                        && tileY >= currentLevel.dynThornsBottomLeft[i].y && tileY < currentLevel.dynThornsTopRight[i].y) {
                    return i;
                }
            }
        }
        return -1;
    }
    @Override public Vec2S[] getDynThornsBottomLeft() { return (currentLevel != null) ? currentLevel.dynThornsBottomLeft : new Vec2S[0]; }
    @Override public Vec2S[] getDynThornsCurrentPos() { return (currentLevel != null) ? currentLevel.dynThornsPos : new Vec2S[0]; }
    @Override public SoundListener getSoundListener() { return soundManager; }
    @Override public boolean isExitUnlocked() { return exitUnlocked; }
}
