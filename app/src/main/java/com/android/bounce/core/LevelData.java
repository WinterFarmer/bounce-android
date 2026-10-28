package com.android.bounce.core;

public class LevelData {
    public int levelId;
    public int ballInitialX;
    public int ballInitialY;
    public int ballSize;
    public int exitX;
    public int exitY;
    public int hoopsTotal;
    public int width;
    public int height;
    public short[][] tiles;

    public int dynThornsCount;
    public Vec2S[] dynThornsBottomLeft;
    public Vec2S[] dynThornsTopRight;
    public Vec2S[] dynThornsSpeed;
    public Vec2S[] dynThornsPos;
}
