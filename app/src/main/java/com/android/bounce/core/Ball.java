package com.android.bounce.core;

import android.graphics.Bitmap;

public class Ball {
    public int posX;
    public int posY;
    public int speedX;
    public int speedY;
    public int ballSize;
    public int halfSize;
    public int checkpointX;
    public int checkpointY;
    public int checkpointBallSize;
    public int state;
    public int boostJump;
    public int powerUpGravity;
    public int powerUpSpeed;
    public int powerUpJump;

    public boolean onGround;
    public boolean isRubberBounce;
    public boolean checkSubPixel;
    public int stepCounter;
    public int inputMask;

    public Bitmap spriteCurrentBall;
    public Bitmap spriteBall;
    public Bitmap spriteBigBall;
    public Bitmap spritePoppedBall;

    private int deathTimer;
    private final GameContext context;

    public static final byte[][] SLOPE_COLLISION = new byte[][] {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    public static final byte[][] BALL_COLLISION = new byte[][] {
            {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0}
    };

    public static final byte[][] BIG_BALL_COLLISION = new byte[][] {
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0}
    };

    public Ball(int startX, int startY, int size, GameContext context) {
        this.posX = startX;
        this.posY = startY;
        this.speedX = 0;
        this.speedY = 0;
        this.context = context;
        this.boostJump = 0;
        this.onGround = false;
        this.isRubberBounce = false;
        this.checkSubPixel = false;
        this.deathTimer = 0;
        this.powerUpSpeed = 0;
        this.powerUpGravity = 0;
        this.powerUpJump = 0;
        this.stepCounter = 0;
        this.state = 0;
        this.inputMask = 0;

        if (size == 12) {
            useRegularBall();
        } else {
            useBigBall();
        }
    }

    public void setSprites(Bitmap ball, Bitmap bigBall, Bitmap popped) {
        this.spriteBall = ball;
        this.spriteBigBall = bigBall;
        this.spritePoppedBall = popped;
        this.spriteCurrentBall = (this.ballSize == 16) ? this.spriteBigBall : this.spriteBall;
    }

    public void setCheckpoint(int tileX, int tileY) {
        this.checkpointX = tileX;
        this.checkpointY = tileY;
        this.checkpointBallSize = this.ballSize;
    }

    public void setInput(int keyFlag) {
        if (keyFlag == 8 || keyFlag == 4 || keyFlag == 2 || keyFlag == 1) {
            this.inputMask |= keyFlag;
        }
    }

    public void clearInput(int keyFlag) {
        if (keyFlag == 8 || keyFlag == 4 || keyFlag == 2 || keyFlag == 1) {
            this.inputMask &= ~keyFlag;
        }
    }

    public void resetAllInputs() {
        this.inputMask &= 0xFFFFFFF0;
    }

    public boolean checkBallCollision(int x, int y) {
        int i = (x - this.halfSize) / 12;
        int j = (y - this.halfSize) / 12;
        int k = (x - 1 + this.halfSize) / 12 + 1;
        int m = (y - 1 + this.halfSize) / 12 + 1;
        for (int tileX = i; tileX < k; tileX++) {
            for (int tileY = j; tileY < m; tileY++) {
                if (!checkTileCollision(x, y, tileY, tileX)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void useBigBall() {
        this.ballSize = 16;
        this.halfSize = 8;
        this.spriteCurrentBall = this.spriteBigBall;

        boolean collides = false;
        for (byte offset = 1; !collides; offset++) {
            collides = true;
            if (checkBallCollision(this.posX, this.posY - offset)) {
                this.posY -= offset;
                continue;
            }
            if (checkBallCollision(this.posX - offset, this.posY - offset)) {
                this.posX -= offset;
                this.posY -= offset;
                continue;
            }
            if (checkBallCollision(this.posX + offset, this.posY - offset)) {
                this.posX += offset;
                this.posY -= offset;
                continue;
            }
            if (checkBallCollision(this.posX, this.posY + offset)) {
                this.posY += offset;
                continue;
            }
            if (checkBallCollision(this.posX - offset, this.posY + offset)) {
                this.posX -= offset;
                this.posY += offset;
                continue;
            }
            if (checkBallCollision(this.posX + offset, this.posY + offset)) {
                this.posX += offset;
                this.posY += offset;
                continue;
            }
            collides = false;
        }
    }

    public void useRegularBall() {
        this.ballSize = 12;
        this.halfSize = 6;
        this.spriteCurrentBall = this.spriteBall;
    }

    public void killBall() {
        if (!this.context.isGodMode()) {
            this.deathTimer = 7;
            this.state = 2;
            this.context.decreaseLife();
            this.powerUpSpeed = 0;
            this.powerUpGravity = 0;
            this.powerUpJump = 0;
            if (this.context.getSoundListener() != null) {
                this.context.getSoundListener().playPop();
            }
        }
    }

    public void onTouchHoop() {
        this.context.addScore(500);
        this.context.hoopScored();
    }

    private void handleSlopeBounce(int tileId) {
        int prevSpeedX = this.speedX;
        switch (tileId) {
            case 35 -> {
                this.speedX = (this.speedX > -this.speedY) ? this.speedX : this.speedY;
                this.speedY = prevSpeedX;
            }
            case 37 -> {
                this.speedX = (-this.speedX > this.speedY) ? this.speedX : this.speedY;
                this.speedY = prevSpeedX;
            }
            case 34 -> {
                this.speedX = (this.speedX < this.speedY) ? this.speedX : -this.speedY;
                this.speedY = -prevSpeedX;
            }
            case 36 -> {
                this.speedX = (this.speedX > this.speedY) ? this.speedX : -this.speedY;
                this.speedY = -prevSpeedX;
            }
            case 31 -> {
                this.speedX = (this.speedX > -this.speedY) ? this.speedX : (this.speedY >> 1);
                this.speedY = prevSpeedX;
            }
            case 33 -> {
                this.speedX = (-this.speedX > this.speedY) ? this.speedX : (this.speedY >> 1);
                this.speedY = prevSpeedX;
            }
            case 30 -> {
                this.speedX = (this.speedX < this.speedY) ? this.speedX : -(this.speedY >> 1);
                this.speedY = -prevSpeedX;
            }
            case 32 -> {
                this.speedX = (this.speedX > this.speedY) ? this.speedX : -(this.speedY >> 1);
                this.speedY = -prevSpeedX;
            }
        }
    }

    public boolean checkWallCollision(int x, int y, int tileY, int tileX) {
        byte b1;
        int n;
        byte b2;
        int i1;
        int i = tileX * 12;
        int j = tileY * 12;
        int k = x - this.halfSize - i;
        int m = y - this.halfSize - j;

        if (k >= 0) {
            b1 = (byte) k;
            n = 12;
        } else {
            b1 = 0;
            n = this.ballSize + k;
        }
        if (m >= 0) {
            b2 = (byte) m;
            i1 = 12;
        } else {
            b2 = 0;
            i1 = this.ballSize + m;
        }

        byte[][] ballCollision = (this.ballSize == 16) ? BIG_BALL_COLLISION : BALL_COLLISION;
        if (n > 12) n = 12;
        if (i1 > 12) i1 = 12;

        for (byte b3 = b1; b3 < n; b3++) {
            for (byte b = b2; b < i1; b++) {
                if (ballCollision[b - m][b3 - k] != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkSlopeCollision(int x, int y, int tileY, int tileX, int tileId) {
        byte b3;
        int n;
        byte b4;
        int i1;
        int i = tileX * 12;
        int j = tileY * 12;
        int k = x - this.halfSize - i;
        int m = y - this.halfSize - j;
        byte b1 = 0;
        byte b2 = 0;

        switch (tileId) {
            case 30, 34 -> {
                b2 = 11;
                b1 = 11;
            }
            case 31, 35 -> b2 = 11;
            case 33, 37 -> b1 = 11;
        }

        if (k >= 0) {
            b3 = (byte) k;
            n = 12;
        } else {
            b3 = 0;
            n = this.ballSize + k;
        }
        if (m >= 0) {
            b4 = (byte) m;
            i1 = 12;
        } else {
            b4 = 0;
            i1 = this.ballSize + m;
        }

        byte[][] ballCollision = (this.ballSize == 16) ? BIG_BALL_COLLISION : BALL_COLLISION;
        if (n > 12) n = 12;
        if (i1 > 12) i1 = 12;

        for (byte b5 = b3; b5 < n; b5++) {
            for (byte b = b4; b < i1; b++) {
                if ((SLOPE_COLLISION[Math.abs(b - b2)][Math.abs(b5 - b1)] & ballCollision[b - m][b5 - k]) != 0) {
                    if (!this.onGround) {
                        handleSlopeBounce(tileId);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkBoundingOverlap(int x, int y, int tileY, int tileX, int tileId) {
        int i = tileX * 12;
        int j = tileY * 12;
        int k = i + 12;
        int m = j + 12;

        switch (tileId) {
            case TileIDs.THORNS_UP, TileIDs.THORNS_DOWN, 9, 13, 14, 17, 18, 21, 22, 43, 45 -> {
                i += 4;
                k -= 4;
            }
            case TileIDs.THORNS_RIGHT, TileIDs.THORNS_LEFT, 15, 16, 19, 20, 23, 24, 44, 46 -> {
                j += 4;
                m -= 4;
            }
        }
        return isBoxIntersecting(x - this.halfSize, y - this.halfSize, x + this.halfSize - 1, y + this.halfSize - 1, i, j, k - 1, m - 1);
    }

    public boolean checkHoopPostCollision(int x, int y, int tileY, int tileX, int tileId) {
        int i = tileX * 12;
        int j = tileY * 12;
        int k = i + 12;
        int m = j + 12;

        switch (tileId) {
            case 15, 19, 23, 27 -> {
                j += 6;
                m -= 6;
                k -= 11;
            }
            case 16, 20, 24, 28 -> {
                j += 6;
                m -= 6;
                i += 11;
            }
            case 13, 17 -> {
                i += 6;
                k -= 6;
                m -= 11;
            }
            case 21, 25 -> {
                m = j;
                j--;
                i += 6;
                k -= 6;
            }
            case 14, 18, 22, 26 -> {
                i += 6;
                k -= 6;
                j += 11;
            }
        }
        return isBoxIntersecting(x - this.halfSize, y - this.halfSize, x + this.halfSize, y + this.halfSize, i, j, k, m);
    }

    public boolean checkTileCollision(int x, int y, int tileY, int tileX) {
        if (tileY >= this.context.getMapHeight() || tileY < 0 || tileX >= this.context.getMapWidth() || tileX < 0) {
            return false;
        }
        if (this.state == 2) {
            return false;
        }

        boolean passable = true;
        short[][] tiles = this.context.getLevelTiles();
        int rawTile = tiles[tileY][tileX];
        int isInWater = rawTile & 0x40;
        int tileId = rawTile & (~0x40) & (~0x80);

        SoundListener snd = this.context.getSoundListener();

        switch (tileId) {
            case TileIDs.BRICK_WALL -> {
                if (checkWallCollision(x, y, tileY, tileX)) {
                    passable = false;
                    this.checkSubPixel = true;
                    break;
                }
                this.checkSubPixel = true;
            }
            case TileIDs.RUBBER_WALL -> {
                if (checkWallCollision(x, y, tileY, tileX)) {
                    this.isRubberBounce = true;
                    passable = false;
                    break;
                }
                this.checkSubPixel = true;
            }
            case 34, 35, 36, 37 -> {
                if (checkSlopeCollision(x, y, tileY, tileX, tileId)) {
                    this.isRubberBounce = true;
                    passable = false;
                    this.checkSubPixel = true;
                }
            }
            case 30, 31, 32, 33 -> {
                if (checkSlopeCollision(x, y, tileY, tileX, tileId)) {
                    passable = false;
                    this.checkSubPixel = true;
                }
            }
            case 10 -> {
                int k = this.context.testPointInsideDynThorns(tileX, tileY);
                if (k != -1) {
                    Vec2S[] bl = this.context.getDynThornsBottomLeft();
                    Vec2S[] cur = this.context.getDynThornsCurrentPos();
                    int m = bl[k].x * 12 + cur[k].x;
                    int n = bl[k].y * 12 + cur[k].y;
                    if (isBoxIntersecting(x - this.halfSize + 1, y - this.halfSize + 1, x + this.halfSize - 1, y + this.halfSize - 1, m + 1, n + 1, m + 24 - 1, n + 24 - 1)) {
                        passable = false;
                        killBall();
                    }
                }
            }
            case TileIDs.THORNS_UP, TileIDs.THORNS_RIGHT, TileIDs.THORNS_DOWN, TileIDs.THORNS_LEFT -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    passable = false;
                    killBall();
                }
            }
            case TileIDs.CRYSTAL -> {
                this.context.addScore(200);
                tiles[this.checkpointY][this.checkpointX] = (short) (0x80 | TileIDs.EMPTY);
                setCheckpoint(tileX, tileY);
                tiles[tileY][tileX] = (short) (0x80 | TileIDs.CRYSTAL_ACTIVE);
                if (snd != null) snd.playPickup();
            }
            case 23 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    if (checkHoopPostCollision(x, y, tileY, tileX, tileId)) {
                        passable = false;
                        break;
                    }
                    onTouchHoop();
                    tiles[tileY][tileX] = (short) (0x80 | 27 | isInWater);
                    tiles[tileY][tileX + 1] = (short) (0x80 | 28 | isInWater);
                    if (snd != null) snd.playUp();
                }
            }
            case 15 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    if (this.ballSize == 16) {
                        passable = false;
                        break;
                    }
                    if (checkHoopPostCollision(x, y, tileY, tileX, tileId)) passable = false;
                    onTouchHoop();
                    tiles[tileY][tileX] = (short) (0x93 | isInWater);
                    tiles[tileY][tileX + 1] = (short) (0x94 | isInWater);
                    if (snd != null) snd.playUp();
                }
            }
            case 24 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    if (checkHoopPostCollision(x, y, tileY, tileX, tileId)) passable = false;
                    onTouchHoop();
                    tiles[tileY][tileX] = (short) (0x9C | isInWater);
                    tiles[tileY][tileX - 1] = (short) (0x9B | isInWater);
                    if (snd != null) snd.playUp();
                }
            }
            case 16 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    if (this.ballSize == 16) {
                        passable = false;
                        break;
                    }
                    if (checkHoopPostCollision(x, y, tileY, tileX, tileId)) passable = false;
                    onTouchHoop();
                    tiles[tileY][tileX] = (short) (0x94 | isInWater);
                    tiles[tileY][tileX - 1] = (short) (0x93 | isInWater);
                    if (snd != null) snd.playUp();
                }
            }
            case 21 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    if (checkHoopPostCollision(x, y, tileY, tileX, tileId)) passable = false;
                    onTouchHoop();
                    tiles[tileY][tileX] = (short) (0x99 | isInWater);
                    tiles[tileY + 1][tileX] = (short) (0x9A | isInWater);
                    if (snd != null) snd.playUp();
                }
            }
            case 13 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    if (this.ballSize == 16) {
                        passable = false;
                        break;
                    }
                    if (checkHoopPostCollision(x, y, tileY, tileX, tileId)) passable = false;
                    onTouchHoop();
                    tiles[tileY][tileX] = (short) (0x91 | isInWater);
                    tiles[tileY + 1][tileX] = (short) (0x92 | isInWater);
                    if (snd != null) snd.playUp();
                }
            }
            case 22 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    onTouchHoop();
                    tiles[tileY][tileX] = (short) (0x9A | isInWater);
                    tiles[tileY - 1][tileX] = (short) (0x99 | isInWater);
                    if (snd != null) snd.playUp();
                }
            }
            case 14 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    if (this.ballSize == 16) {
                        passable = false;
                        break;
                    }
                    onTouchHoop();
                    tiles[tileY][tileX] = (short) (0x92 | isInWater);
                    tiles[tileY - 1][tileX] = (short) (0x91 | isInWater);
                    if (snd != null) snd.playUp();
                }
            }
            case 17, 19, 20 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    if (this.ballSize == 16) {
                        passable = false;
                        break;
                    }
                    if (checkHoopPostCollision(x, y, tileY, tileX, tileId)) passable = false;
                }
            }
            case 25, 27, 28 -> {
                if (checkHoopPostCollision(x, y, tileY, tileX, tileId)) passable = false;
            }
            case 18 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId) && this.ballSize == 16) {
                    passable = false;
                }
            }
            case 9 -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    if (this.context.isExitUnlocked()) {
                        this.context.triggerGameLevelComplete();
                        if (snd != null) snd.playPickup();
                        break;
                    }
                    passable = false;
                }
            }
            case 29 -> {
                this.context.addScore(1000);
                this.context.addLife();
                tiles[tileY][tileX] = (short) (0x80 | TileIDs.EMPTY);
                if (snd != null) snd.playPickup();
            }
            case 39, 40, 41, 42 -> {
                passable = false;
                if (this.ballSize == 16) useRegularBall();
            }
            case TileIDs.PUMPER_UP, TileIDs.PUMPER_RIGHT, TileIDs.PUMPER_DOWN, TileIDs.PUMPER_LEFT -> {
                if (checkBoundingOverlap(x, y, tileY, tileX, tileId)) {
                    passable = false;
                    if (this.ballSize == 12) useBigBall();
                }
            }
            case TileIDs.POWER_UP_GRAVITY_UP, TileIDs.POWER_UP_GRAVITY_RIGHT, TileIDs.POWER_UP_GRAVITY_DOWN, TileIDs.POWER_UP_GRAVITY_LEFT -> {
                this.powerUpGravity = 300;
                this.onGround = false;
                passable = false;
                if (snd != null) snd.playPickup();
            }
            case 51, 52, 53, 54 -> {
                this.powerUpJump = 300;
                passable = false;
                if (snd != null) snd.playPickup();
            }
            case 38 -> {
                this.powerUpSpeed = 300;
                passable = false;
                if (snd != null) snd.playPickup();
            }
        }
        return passable;
    }

    public void updatePhysics() {
        int terminalVelocity = 0;
        int gravityAcc = 0;
        int maxMoveSpeed = 50;
        boolean invertedGravity = false;

        if (this.state == 2) {
            this.deathTimer--;
            if (this.deathTimer == 0) {
                this.state = 1;
            }
            return;
        }

        int tileX = this.posX / 12;
        int tileY = this.posY / 12;
        short[][] tiles = this.context.getLevelTiles();
        boolean isInWater = false;
        if (tileY >= 0 && tileY < this.context.getMapHeight() && tileX >= 0 && tileX < this.context.getMapWidth()) {
            isInWater = (tiles[tileY][tileX] & 0x40) != 0;
        }

        if (isInWater) {
            if (this.ballSize == 16) {
                terminalVelocity = -30;
                gravityAcc = -2;
                if (this.onGround) this.speedY = -10;
            } else {
                terminalVelocity = 42;
                gravityAcc = 6;
            }
        } else if (this.ballSize == 16) {
            terminalVelocity = 38;
            gravityAcc = 3;
        } else {
            terminalVelocity = 80;
            gravityAcc = 4;
        }

        if (this.powerUpGravity != 0) {
            invertedGravity = true;
            terminalVelocity *= -1;
            gravityAcc *= -1;
            this.powerUpGravity--;
            if (this.powerUpGravity == 0) {
                invertedGravity = false;
                this.onGround = false;
                terminalVelocity *= -1;
                gravityAcc *= -1;
            }
        }

        if (this.powerUpJump != 0) {
            if (-1 * Math.abs(this.boostJump) > -80) {
                this.boostJump = invertedGravity ? 80 : -80;
            }
            this.powerUpJump--;
        }

        this.stepCounter++;
        if (this.stepCounter == 3) this.stepCounter = 0;

        if (this.speedY < -150) this.speedY = -150;
        else if (this.speedY > 150) this.speedY = 150;

        if (this.speedX < -150) this.speedX = -150;
        else if (this.speedX > 150) this.speedX = 150;

        if (this.speedY < 10 && this.speedY > 0 && !isInWater && !invertedGravity) {
            this.speedY = 10;
        }

        for (byte b2 = 0; b2 < Math.abs(this.speedY) / 10; b2++) {
            byte dy = 0;
            if (this.speedY != 0) dy = (byte) ((this.speedY < 0) ? -1 : 1);

            if (checkBallCollision(this.posX, this.posY + dy)) {
                this.posY += dy;
                this.onGround = false;
                if (terminalVelocity == -30) {
                    tileY = this.posY / 12;
                    if ((tiles[tileY][tileX] & 0x40) == 0) {
                        this.speedY >>= 1;
                        if (this.speedY <= 10 && this.speedY >= -10) this.speedY = 0;
                    }
                }
            } else {
                if (this.checkSubPixel && this.speedX < 10 && this.stepCounter == 0) {
                    byte dx = 1;
                    if (checkBallCollision(this.posX + dx, this.posY + dy)) {
                        this.posX += dx;
                        this.posY += dy;
                        this.checkSubPixel = false;
                    } else if (checkBallCollision(this.posX - dx, this.posY + dy)) {
                        this.posX -= dx;
                        this.posY += dy;
                        this.checkSubPixel = false;
                    }
                }
                if (dy > 0 || (invertedGravity && dy < 0)) {
                    this.speedY = this.speedY * -1 / 2;
                    this.onGround = true;
                    if (this.isRubberBounce && (this.inputMask & 0x8) != 0) {
                        this.isRubberBounce = false;
                        this.boostJump += invertedGravity ? 10 : -10;
                    } else if (this.powerUpJump == 0) {
                        this.boostJump = 0;
                    }
                    if (this.speedY < 10 && this.speedY > -10) {
                        if (invertedGravity) {
                            this.speedY = -10;
                            break;
                        }
                        this.speedY = 10;
                    }
                    break;
                }
                if (dy < 0 || (invertedGravity && dy > 0)) {
                    if (invertedGravity) {
                        this.speedY = -20;
                    } else {
                        this.speedY = -this.speedY >> 1;
                    }
                }
            }
        }

        if (invertedGravity) {
            if (gravityAcc == -2 && this.speedY < terminalVelocity) {
                this.speedY += gravityAcc;
                if (this.speedY > terminalVelocity) this.speedY = terminalVelocity;
            } else if (!this.onGround && this.speedY > terminalVelocity) {
                this.speedY += gravityAcc;
                if (this.speedY < terminalVelocity) this.speedY = terminalVelocity;
            }
        } else if (gravityAcc == -2 && this.speedY > terminalVelocity) {
            this.speedY += gravityAcc;
            if (this.speedY < terminalVelocity) this.speedY = terminalVelocity;
        } else if (!this.onGround && this.speedY < terminalVelocity) {
            this.speedY += gravityAcc;
            if (this.speedY > terminalVelocity) this.speedY = terminalVelocity;
        }

        if (this.powerUpSpeed != 0) {
            maxMoveSpeed = 100;
            this.powerUpSpeed--;
        }

        if ((this.inputMask & 0x2) != 0 && this.speedX < maxMoveSpeed) {
            this.speedX += 6;
        } else if ((this.inputMask & 0x1) != 0 && this.speedX > -maxMoveSpeed) {
            this.speedX -= 6;
        } else if (this.speedX > 0) {
            this.speedX -= 4;
        } else if (this.speedX < 0) {
            this.speedX += 4;
        }

        if (this.ballSize == 16 && this.powerUpJump == 0) {
            this.boostJump += invertedGravity ? 5 : -5;
        }

        if (this.onGround && (this.inputMask & 0x8) != 0) {
            this.speedY = invertedGravity ? (67 + this.boostJump) : (-67 + this.boostJump);
            this.onGround = false;
        }

        int absSpeedX = Math.abs(this.speedX);
        int stepsX = absSpeedX / 10;
        for (byte b3 = 0; b3 < stepsX; b3++) {
            byte dx = 0;
            if (this.speedX != 0) dx = (byte) ((this.speedX < 0) ? -1 : 1);

            if (checkBallCollision(this.posX + dx, this.posY)) {
                this.posX += dx;
            } else if (this.checkSubPixel) {
                this.checkSubPixel = false;
                byte dy = (byte) (invertedGravity ? 1 : -1);
                if (checkBallCollision(this.posX + dx, this.posY + dy)) {
                    this.posX += dx;
                    this.posY += dy;
                } else if (checkBallCollision(this.posX + dx, this.posY - dy)) {
                    this.posX += dx;
                    this.posY -= dy;
                } else {
                    this.speedX = -(this.speedX >> 1);
                }
            }
        }
    }

    public static boolean isBoxIntersecting(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        return (x1 <= x4 && y1 <= y4 && x3 <= x2 && y3 <= y2);
    }

    public void resolveSpawnCollision() {
        if (checkBallCollision(this.posX, this.posY)) {
            return;
        }
        for (int d = 1; d <= 12; d++) {
            if (checkBallCollision(this.posX, this.posY - d)) { this.posY -= d; return; }
            if (checkBallCollision(this.posX - d, this.posY)) { this.posX -= d; return; }
            if (checkBallCollision(this.posX + d, this.posY)) { this.posX += d; return; }
            if (checkBallCollision(this.posX - d, this.posY - d)) { this.posX -= d; this.posY -= d; return; }
            if (checkBallCollision(this.posX + d, this.posY - d)) { this.posX += d; this.posY -= d; return; }
            if (checkBallCollision(this.posX, this.posY + d)) { this.posY += d; return; }
            if (checkBallCollision(this.posX - d, this.posY + d)) { this.posX -= d; this.posY += d; return; }
            if (checkBallCollision(this.posX + d, this.posY + d)) { this.posX += d; this.posY += d; return; }
        }
    }

    public void respawn() {
        this.posX = this.checkpointX * 12 + 6;
        this.posY = this.checkpointY * 12 + 6;
        this.speedX = 0;
        this.speedY = 0;
        this.boostJump = 0;
        this.powerUpGravity = 0;
        this.powerUpSpeed = 0;
        this.powerUpJump = 0;
        this.state = 0;
        this.onGround = false;
        this.isRubberBounce = false;
        this.checkSubPixel = false;
        this.deathTimer = 0;

        if (this.checkpointBallSize == 16) {
            useBigBall();
        } else {
            useRegularBall();
        }

        resolveSpawnCollision();
    }
}
