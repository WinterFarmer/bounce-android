package com.android.bounce;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        gameView = new GameView(this);
        setContentView(gameView);

        updateScreenMode();
    }

    public void applyFullscreenMode(boolean fullscreen) {
        updateScreenMode();
    }

    private void updateScreenMode() {
        runOnUiThread(() -> {
            boolean isFs = (gameView != null && gameView.isFullscreen());
            boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
            View decorView = getWindow().getDecorView();

            if (isFs) {
                // Mode Fullscreen ON: Tembus notch/poni kamera total di semua orientasi
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    getWindow().getAttributes().layoutInDisplayCutoutMode =
                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                }
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                );
            } else {
                // Mode Fullscreen OFF: Sembunyikan kamera (black bar tetap ada)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    getWindow().getAttributes().layoutInDisplayCutoutMode =
                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                }

                if (isLandscape) {
                    // Di Landscape: Sembunyikan status bar & navigasi, tapi sisakan black bar kamera
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                    );
                } else {
                    // Di Portrait: Tampilkan status bar dan navigasi normal
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateScreenMode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateScreenMode();
        }
    }

    @Override
    public void onBackPressed() {
        if (gameView != null && gameView.handleBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
