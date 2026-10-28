package com.android.bounce.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.android.bounce.core.Ball;
import com.android.bounce.core.HoopLookup;
import com.android.bounce.core.LevelData;
import com.android.bounce.core.TileIDs;

public class GameRenderer {
    public static final int SKY_COLOR = 0xFFB0E0F0;
    public static final int WATER_COLOR = 0xFF1060B0;
    public static final int HUD_BG_COLOR = 0xFF0853AA;

    public Bitmap[] sprites = new Bitmap[67];
    public Bitmap spriteDynThorn;
    public Bitmap imageBallIcon;
    public Bitmap imageHoopIcon;

    public Bitmap deflater90, deflater180, deflater270;
    public Bitmap pumper90, pumper180, pumper270;
    public Bitmap gravity90, gravity180, gravity270;
    public Bitmap jump90, jump180, jump270;

    private final Paint paint = new Paint();

    // Bitmap Font Matrix 4x7 untuk Angka 0 - 9
    private static final byte[][] FONT_NUMBERS = {
        {0x6, 0x9, 0x9, 0x9, 0x9, 0x9, 0x6}, // 0
        {0x2, 0x6, 0x2, 0x2, 0x2, 0x2, 0x7}, // 1
        {0x6, 0x9, 0x1, 0x2, 0x4, 0x8, 0xF}, // 2
        {0xE, 0x1, 0x1, 0x6, 0x1, 0x1, 0xE}, // 3
        {0x9, 0x9, 0x9, 0xF, 0x1, 0x1, 0x1}, // 4
        {0xF, 0x8, 0x8, 0xE, 0x1, 0x1, 0xE}, // 5
        {0x6, 0x8, 0x8, 0xE, 0x9, 0x9, 0x6}, // 6
        {0xF, 0x1, 0x2, 0x2, 0x4, 0x4, 0x4}, // 7
        {0x6, 0x9, 0x9, 0x6, 0x9, 0x9, 0x6}, // 8
        {0x6, 0x9, 0x9, 0x7, 0x1, 0x1, 0x6}  // 9
    };

    public GameRenderer(Context context) {
        paint.setFilterBitmap(false);
        paint.setAntiAlias(false);
        paint.setDither(false);
        loadSprites(context);
    }

    private void loadSprites(Context context) {
        Bitmap ss = SpriteSheet.loadBitmapFromAsset(context, "icons/objects_nm.png");
        if (ss == null) return;

        sprites[0] = SpriteSheet.createTile(ss, 1, 0, 0); // Brick
        sprites[1] = SpriteSheet.createTile(ss, 1, 2, 0); // Rubber
        sprites[2] = SpriteSheet.createTile(ss, 0, 3, SKY_COLOR); // Thorns Up
        sprites[3] = SpriteSheet.modifyBitmap(sprites[2], SpriteSheet.FLIP_VERTICAL);
        sprites[4] = SpriteSheet.modifyBitmap(sprites[2], SpriteSheet.ROTATE_90);
        sprites[5] = SpriteSheet.modifyBitmap(sprites[2], SpriteSheet.ROTATE_270);

        sprites[6] = SpriteSheet.createTile(ss, 0, 3, WATER_COLOR);
        sprites[7] = SpriteSheet.modifyBitmap(sprites[6], SpriteSheet.FLIP_VERTICAL);
        sprites[8] = SpriteSheet.modifyBitmap(sprites[6], SpriteSheet.ROTATE_90);
        sprites[9] = SpriteSheet.modifyBitmap(sprites[6], SpriteSheet.ROTATE_270);

        sprites[10] = SpriteSheet.createTile(ss, 0, 4, 0); // Crystal
        sprites[11] = SpriteSheet.createTile(ss, 3, 4, 0); // Crystal Active
        sprites[12] = SpriteSheet.mirrorExitTile(SpriteSheet.createTile(ss, 2, 3, 0)); // Exit Gate

        sprites[14] = SpriteSheet.createTile(ss, 0, 5, 0);
        sprites[13] = SpriteSheet.modifyBitmap(sprites[14], SpriteSheet.FLIP_VERTICAL);
        sprites[15] = SpriteSheet.modifyBitmap(sprites[13], SpriteSheet.FLIP_HORIZONTAL);
        sprites[16] = SpriteSheet.modifyBitmap(sprites[14], SpriteSheet.FLIP_HORIZONTAL);

        sprites[18] = SpriteSheet.createTile(ss, 1, 5, 0);
        sprites[17] = SpriteSheet.modifyBitmap(sprites[18], SpriteSheet.FLIP_VERTICAL);
        sprites[19] = SpriteSheet.modifyBitmap(sprites[17], SpriteSheet.FLIP_HORIZONTAL);
        sprites[20] = SpriteSheet.modifyBitmap(sprites[18], SpriteSheet.FLIP_HORIZONTAL);

        sprites[22] = SpriteSheet.createTile(ss, 2, 5, 0);
        sprites[21] = SpriteSheet.modifyBitmap(sprites[22], SpriteSheet.FLIP_VERTICAL);
        sprites[23] = SpriteSheet.modifyBitmap(sprites[21], SpriteSheet.FLIP_HORIZONTAL);
        sprites[24] = SpriteSheet.modifyBitmap(sprites[22], SpriteSheet.FLIP_HORIZONTAL);

        sprites[26] = SpriteSheet.createTile(ss, 3, 5, 0);
        sprites[25] = SpriteSheet.modifyBitmap(sprites[26], SpriteSheet.FLIP_VERTICAL);
        sprites[27] = SpriteSheet.modifyBitmap(sprites[25], SpriteSheet.FLIP_HORIZONTAL);
        sprites[28] = SpriteSheet.modifyBitmap(sprites[26], SpriteSheet.FLIP_HORIZONTAL);

        sprites[29] = SpriteSheet.modifyBitmap(sprites[14], SpriteSheet.ROTATE_270);
        sprites[30] = SpriteSheet.modifyBitmap(sprites[29], SpriteSheet.FLIP_VERTICAL);
        sprites[31] = SpriteSheet.modifyBitmap(sprites[29], SpriteSheet.FLIP_HORIZONTAL);
        sprites[32] = SpriteSheet.modifyBitmap(sprites[30], SpriteSheet.FLIP_HORIZONTAL);

        sprites[33] = SpriteSheet.modifyBitmap(sprites[18], SpriteSheet.ROTATE_270);
        sprites[34] = SpriteSheet.modifyBitmap(sprites[33], SpriteSheet.FLIP_VERTICAL);
        sprites[35] = SpriteSheet.modifyBitmap(sprites[33], SpriteSheet.FLIP_HORIZONTAL);
        sprites[36] = SpriteSheet.modifyBitmap(sprites[34], SpriteSheet.FLIP_HORIZONTAL);

        sprites[37] = SpriteSheet.modifyBitmap(sprites[22], SpriteSheet.ROTATE_270);
        sprites[38] = SpriteSheet.modifyBitmap(sprites[37], SpriteSheet.FLIP_VERTICAL);
        sprites[39] = SpriteSheet.modifyBitmap(sprites[37], SpriteSheet.FLIP_HORIZONTAL);
        sprites[40] = SpriteSheet.modifyBitmap(sprites[38], SpriteSheet.FLIP_HORIZONTAL);

        sprites[41] = SpriteSheet.modifyBitmap(sprites[26], SpriteSheet.ROTATE_270);
        sprites[42] = SpriteSheet.modifyBitmap(sprites[41], SpriteSheet.FLIP_VERTICAL);
        sprites[43] = SpriteSheet.modifyBitmap(sprites[41], SpriteSheet.FLIP_HORIZONTAL);
        sprites[44] = SpriteSheet.modifyBitmap(sprites[42], SpriteSheet.FLIP_HORIZONTAL);

        sprites[45] = SpriteSheet.createTile(ss, 3, 3, 0);
        sprites[46] = SpriteSheet.createTile(ss, 1, 3, 0);
        sprites[47] = SpriteSheet.createTile(ss, 2, 0, 0);
        sprites[48] = SpriteSheet.createTile(ss, 0, 1, 0);
        sprites[49] = SpriteSheet.mirror16x16(SpriteSheet.createTile(ss, 3, 0, 0));

        sprites[50] = SpriteSheet.createTile(ss, 3, 1, 0);
        deflater90 = SpriteSheet.modifyBitmap(sprites[50], SpriteSheet.ROTATE_90);
        deflater180 = SpriteSheet.modifyBitmap(sprites[50], SpriteSheet.ROTATE_180);
        deflater270 = SpriteSheet.modifyBitmap(sprites[50], SpriteSheet.ROTATE_270);

        sprites[51] = SpriteSheet.createTile(ss, 2, 4, 0);
        pumper90 = SpriteSheet.modifyBitmap(sprites[51], SpriteSheet.ROTATE_90);
        pumper180 = SpriteSheet.modifyBitmap(sprites[51], SpriteSheet.ROTATE_180);
        pumper270 = SpriteSheet.modifyBitmap(sprites[51], SpriteSheet.ROTATE_270);

        sprites[52] = SpriteSheet.createTile(ss, 3, 2, 0);
        gravity90 = SpriteSheet.modifyBitmap(sprites[52], SpriteSheet.ROTATE_90);
        gravity180 = SpriteSheet.modifyBitmap(sprites[52], SpriteSheet.ROTATE_180);
        gravity270 = SpriteSheet.modifyBitmap(sprites[52], SpriteSheet.ROTATE_270);

        sprites[53] = SpriteSheet.createTile(ss, 1, 1, 0);
        sprites[54] = SpriteSheet.createTile(ss, 2, 2, 0);
        jump90 = SpriteSheet.modifyBitmap(sprites[54], SpriteSheet.ROTATE_90);
        jump180 = SpriteSheet.modifyBitmap(sprites[54], SpriteSheet.ROTATE_180);
        jump270 = SpriteSheet.modifyBitmap(sprites[54], SpriteSheet.ROTATE_270);

        sprites[55] = SpriteSheet.createTile(ss, 0, 0, SKY_COLOR);
        sprites[56] = SpriteSheet.modifyBitmap(sprites[55], SpriteSheet.ROTATE_90);
        sprites[57] = SpriteSheet.modifyBitmap(sprites[55], SpriteSheet.ROTATE_180);
        sprites[58] = SpriteSheet.modifyBitmap(sprites[55], SpriteSheet.ROTATE_270);

        sprites[59] = SpriteSheet.createTile(ss, 0, 0, WATER_COLOR);
        sprites[60] = SpriteSheet.modifyBitmap(sprites[59], SpriteSheet.ROTATE_90);
        sprites[61] = SpriteSheet.modifyBitmap(sprites[59], SpriteSheet.ROTATE_180);
        sprites[62] = SpriteSheet.modifyBitmap(sprites[59], SpriteSheet.ROTATE_270);

        sprites[63] = SpriteSheet.createTile(ss, 0, 2, 0);
        sprites[64] = SpriteSheet.modifyBitmap(sprites[63], SpriteSheet.ROTATE_90);
        sprites[65] = SpriteSheet.modifyBitmap(sprites[63], SpriteSheet.ROTATE_180);
        sprites[66] = SpriteSheet.modifyBitmap(sprites[63], SpriteSheet.ROTATE_270);

        imageBallIcon = SpriteSheet.createTile(ss, 2, 1, 0);
        imageHoopIcon = SpriteSheet.createTile(ss, 1, 4, 0);

        spriteDynThorn = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);
        Canvas dtc = new Canvas(spriteDynThorn);
        dtc.drawBitmap(sprites[46], 0, 0, paint);
        dtc.drawBitmap(SpriteSheet.modifyBitmap(sprites[46], SpriteSheet.FLIP_HORIZONTAL), 12, 0, paint);
        dtc.drawBitmap(SpriteSheet.modifyBitmap(sprites[46], SpriteSheet.ROTATE_180), 12, 12, paint);
        dtc.drawBitmap(SpriteSheet.modifyBitmap(sprites[46], SpriteSheet.FLIP_VERTICAL), 0, 12, paint);
    }

    public void render(Canvas c, LevelData level, Ball ball, int camX, int camY, int lives, int hoopsLeft, int score, int exitOffset, int boostTimer) {
        c.drawColor(SKY_COLOR);

        int startTileX = camX / 12;
        int startTileY = camY / 12;
        int offsetX = -(camX % 12);
        int offsetY = -(camY % 12);

        for (int ty = 0; ty <= 9; ty++) {
            for (int tx = 0; tx <= 11; tx++) {
                int mapY = startTileY + ty;
                int mapX = startTileX + tx;
                int drawX = offsetX + tx * 12;
                int drawY = offsetY + ty * 12;

                if (drawY >= 96) continue;

                if (mapY < 0 || mapY >= level.height || mapX < 0 || mapX >= level.width) {
                    if (sprites[0] != null) c.drawBitmap(sprites[0], drawX, drawY, paint);
                    continue;
                }

                int raw = level.tiles[mapY][mapX];
                boolean inWater = (raw & 0x40) != 0;
                int id = raw & (~0x40) & (~0x80);

                if (inWater) {
                    paint.setColor(WATER_COLOR);
                    c.drawRect(drawX, drawY, drawX + 12, drawY + 12, paint);
                }

                drawSingleTile(c, id, inWater, drawX, drawY, mapX, mapY, level, exitOffset);
            }
        }

        if (level.dynThornsCount > 0 && level.dynThornsPos != null) {
            for (int i = 0; i < level.dynThornsCount; i++) {
                int dtx = level.dynThornsBottomLeft[i].x * 12 + level.dynThornsPos[i].x - camX;
                int dty = level.dynThornsBottomLeft[i].y * 12 + level.dynThornsPos[i].y - camY;
                if (spriteDynThorn != null) {
                    c.drawBitmap(spriteDynThorn, dtx, dty, paint);
                }
            }
        }

        if (ball != null) {
            int bx = ball.posX - camX;
            int by = ball.posY - camY;
            Bitmap ballBmp = (ball.state == 2) ? sprites[48] : ((ball.ballSize == 16) ? sprites[49] : sprites[47]);
            if (ballBmp != null) {
                c.drawBitmap(ballBmp, bx - ball.halfSize, by - ball.halfSize, paint);
            }
        }

        renderHUD(c, lives, hoopsLeft, score, boostTimer);
    }

    private void drawSingleTile(Canvas c, int id, boolean inWater, int dx, int dy, int mapX, int mapY, LevelData level, int exitOffset) {
        Bitmap b = null;
        switch (id) {
            case TileIDs.BRICK_WALL -> b = sprites[0];
            case TileIDs.RUBBER_WALL -> b = sprites[1];
            case TileIDs.THORNS_UP -> b = inWater ? sprites[6] : sprites[2];
            case TileIDs.THORNS_RIGHT -> b = inWater ? sprites[9] : sprites[5];
            case TileIDs.THORNS_DOWN -> b = inWater ? sprites[7] : sprites[3];
            case TileIDs.THORNS_LEFT -> b = inWater ? sprites[8] : sprites[4];
            case TileIDs.CRYSTAL -> b = sprites[10];
            case TileIDs.CRYSTAL_ACTIVE -> b = sprites[11];
            case 9 -> {
                if (sprites[12] != null && level != null) {
                    int relX = (mapX - level.exitX) * 12;
                    int relY = (mapY - level.exitY) * 12;
                    c.save();
                    c.clipRect(dx, dy, dx + 12, dy + 12);
                    c.drawBitmap(sprites[12], dx - relX, dy - relY - exitOffset, paint);
                    c.restore();
                }
            }
            case 29 -> b = sprites[45];
            case 30 -> b = inWater ? sprites[61] : sprites[57];
            case 31 -> b = inWater ? sprites[60] : sprites[56];
            case 32 -> b = inWater ? sprites[59] : sprites[55];
            case 33 -> b = inWater ? sprites[62] : sprites[58];
            case 34 -> b = sprites[65];
            case 35 -> b = sprites[64];
            case 36 -> b = sprites[63];
            case 37 -> b = sprites[66];
            case 38 -> b = sprites[53];
            case 39 -> b = sprites[50];
            case 40 -> b = deflater270;
            case 41 -> b = deflater180;
            case 42 -> b = deflater90;
            case 43 -> b = sprites[51];
            case 44 -> b = pumper270;
            case 45 -> b = pumper180;
            case 46 -> b = pumper90;
            case 47 -> b = sprites[52];
            case 48 -> b = gravity270;
            case 49 -> b = gravity180;
            case 50 -> b = gravity90;
            case 51 -> b = sprites[54];
            case 52 -> b = jump270;
            case 53 -> b = jump180;
            case 54 -> b = jump90;
            default -> {
                if (id >= 13 && id <= 28) {
                    int idx = id - 13;
                    if (idx < HoopLookup.a.length && sprites[HoopLookup.a[idx]] != null) {
                        c.drawBitmap(sprites[HoopLookup.a[idx]], dx, dy, paint);
                    }
                    if (idx < HoopLookup.b.length && sprites[HoopLookup.b[idx]] != null) {
                        c.drawBitmap(sprites[HoopLookup.b[idx]], dx, dy, paint);
                    }
                }
            }
        }
        if (b != null) c.drawBitmap(b, dx, dy, paint);
    }

    private void renderHUD(Canvas c, int lives, int hoopsLeft, int score, int boostTimer) {
        paint.setColor(HUD_BG_COLOR);
        c.drawRect(0, 96, 128, 128, paint);

        if (imageBallIcon != null) {
            for (int i = 0; i < lives && i < 5; i++) {
                c.drawBitmap(imageBallIcon, 4 + i * 11, 99, paint);
            }
        }

        if (imageHoopIcon != null) {
            for (int i = 0; i < hoopsLeft && i < 10; i++) {
                c.drawBitmap(imageHoopIcon, 4 + i * 10, 113, paint);
            }
        }

        // Render Angka Skor dengan Pixel Font Retro (7 Digit)
        String scoreStr = String.format("%07d", score);
        int startX = 74;
        int startY = 104;
        paint.setColor(Color.WHITE);

        for (int i = 0; i < scoreStr.length(); i++) {
            int digit = scoreStr.charAt(i) - '0';
            if (digit >= 0 && digit <= 9) {
                drawPixelDigit(c, digit, startX + (i * 6), startY);
            }
        }

        // Indikator Bar Oranye Durasi Booster (Power-up)
        if (boostTimer > 0) {
            paint.setColor(0xFFFF9813);
            int barHeight = Math.min(28, (boostTimer * 28) / 300);
            c.drawRect(1, 126 - barHeight, 3, 126, paint);
        }
    }

    private void drawPixelDigit(Canvas c, int digit, int x, int y) {
        byte[] rows = FONT_NUMBERS[digit];
        for (int r = 0; r < 7; r++) {
            int rowBits = rows[r];
            for (int col = 0; col < 4; col++) {
                if ((rowBits & (1 << (3 - col))) != 0) {
                    c.drawRect(x + col, y + r, x + col + 1, y + r + 1, paint);
                }
            }
        }
    }
}
