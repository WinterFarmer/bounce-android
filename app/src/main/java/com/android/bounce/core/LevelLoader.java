package com.android.bounce.core;

import android.content.Context;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Locale;

public class LevelLoader {

    public static LevelData loadLevel(Context context, int levelId) {
        LevelData data = new LevelData();
        data.levelId = levelId;
        String fileName = String.format(Locale.US, "levels/J2MElvl.%03d", levelId);

        try (InputStream is = context.getAssets().open(fileName);
             DataInputStream dis = new DataInputStream(is)) {

            data.ballInitialX = dis.read();
            data.ballInitialY = dis.read();
            int isBig = dis.read();
            data.ballSize = (isBig == 0) ? 12 : 16;
            data.exitX = dis.read();
            data.exitY = dis.read();
            data.hoopsTotal = dis.read();
            data.width = dis.read();
            data.height = dis.read();

            data.tiles = new short[data.height][data.width];
            for (int r = 0; r < data.height; r++) {
                for (int c = 0; c < data.width; c++) {
                    data.tiles[r][c] = (short) dis.read();
                }
            }

            data.dynThornsCount = dis.read();
            if (data.dynThornsCount > 0) {
                int avail = is.available();
                int bytesPerThorn = 10;
                if (avail > 0 && avail % data.dynThornsCount == 0) {
                    bytesPerThorn = avail / data.dynThornsCount;
                }

                data.dynThornsBottomLeft = new Vec2S[data.dynThornsCount];
                data.dynThornsTopRight = new Vec2S[data.dynThornsCount];
                data.dynThornsSpeed = new Vec2S[data.dynThornsCount];
                data.dynThornsPos = new Vec2S[data.dynThornsCount];

                for (int i = 0; i < data.dynThornsCount; i++) {
                    short blX = (short) dis.read();
                    short blY = (short) dis.read();
                    data.dynThornsBottomLeft[i] = new Vec2S(blX, blY);

                    short trX = (short) dis.read();
                    short trY = (short) dis.read();
                    data.dynThornsTopRight[i] = new Vec2S(trX, trY);

                    if (bytesPerThorn == 10) {
                        dis.read();
                        dis.read();
                    }

                    short spdX = (short) (byte) dis.read();
                    short spdY = (short) (byte) dis.read();
                    if (spdX == 0 && spdY == 0) spdX = 1;
                    data.dynThornsSpeed[i] = new Vec2S(spdX, spdY);

                    short posX = (short) (byte) dis.read();
                    short posY = (short) (byte) dis.read();
                    data.dynThornsPos[i] = new Vec2S((short) Math.max(0, posX), (short) Math.max(0, posY));
                }
            }
            return data;
        } catch (Exception e) {
            return null;
        }
    }
}
