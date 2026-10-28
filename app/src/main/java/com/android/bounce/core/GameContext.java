package com.android.bounce.core;

public interface GameContext {
    int getMapWidth();
    int getMapHeight();
    short[][] getLevelTiles();
    boolean isGodMode();
    void decreaseLife();
    void addLife();
    void addScore(int points);
    void hoopScored();
    void triggerGameLevelComplete();
    int testPointInsideDynThorns(int tileX, int tileY);
    Vec2S[] getDynThornsBottomLeft();
    Vec2S[] getDynThornsCurrentPos();
    SoundListener getSoundListener();
    boolean isExitUnlocked();
}
