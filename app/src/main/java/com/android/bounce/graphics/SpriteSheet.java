package com.android.bounce.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import java.io.InputStream;

public class SpriteSheet {
    public static final int FLIP_HORIZONTAL = 0;
    public static final int FLIP_VERTICAL = 1;
    public static final int FLIP_BOTH = 2;
    public static final int ROTATE_90 = 3;
    public static final int ROTATE_180 = 4;
    public static final int ROTATE_270 = 5;

    public static Bitmap loadBitmapFromAsset(Context context, String path) {
        try {
            InputStream is = context.getAssets().open(path);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap createTile(Bitmap src, int tileX, int tileY, int bgColor) {
        Bitmap tile = Bitmap.createBitmap(12, 12, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tile);
        if (bgColor != 0) {
            canvas.drawColor(bgColor);
        }
        if (src != null) {
            Paint paint = new Paint();
            paint.setFilterBitmap(false);
            paint.setAntiAlias(false);
            canvas.drawBitmap(src, -tileX * 12f, -tileY * 12f, paint);
        }
        return tile;
    }

    public static Bitmap modifyBitmap(Bitmap src, int manipulation) {
        if (src == null) return null;
        Matrix matrix = new Matrix();
        switch (manipulation) {
            case FLIP_HORIZONTAL:
                matrix.preScale(-1.0f, 1.0f);
                break;
            case FLIP_VERTICAL:
                matrix.preScale(1.0f, -1.0f);
                break;
            case FLIP_BOTH:
                matrix.preScale(-1.0f, -1.0f);
                break;
            case ROTATE_90:
                // Nokia 90 CCW = Android 270 CW
                matrix.postRotate(270.0f);
                break;
            case ROTATE_180:
                matrix.postRotate(180.0f);
                break;
            case ROTATE_270:
                // Nokia 270 CCW = Android 90 CW
                matrix.postRotate(90.0f);
                break;
        }
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, false);
    }

    public static Bitmap mirror16x16(Bitmap src) {
        Bitmap result = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        canvas.drawBitmap(src, -4f, -4f, paint);
        Bitmap flippedH = modifyBitmap(src, FLIP_HORIZONTAL);
        canvas.drawBitmap(flippedH, 8f, -4f, paint);
        Bitmap flippedV = modifyBitmap(src, FLIP_VERTICAL);
        canvas.drawBitmap(flippedV, -4f, 8f, paint);
        Bitmap rotated180 = modifyBitmap(src, ROTATE_180);
        canvas.drawBitmap(rotated180, 8f, 8f, paint);

        return result;
    }

    public static Bitmap mirrorExitTile(Bitmap src) {
        Bitmap result = Bitmap.createBitmap(24, 48, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        paint.setColor(0xFFB0E0F0);
        canvas.drawRect(0, 0, 24, 48, paint);
        paint.setColor(0xFFFC9D9E);
        canvas.drawRect(4, 0, 20, 48, paint);
        paint.setColor(0xFFE33A3F);
        canvas.drawRect(6, 0, 16, 48, paint);
        paint.setColor(0xFFC2848E);
        canvas.drawRect(10, 0, 14, 48, paint);

        canvas.drawBitmap(src, 0f, 0f, paint);
        Bitmap flippedH = modifyBitmap(src, FLIP_HORIZONTAL);
        canvas.drawBitmap(flippedH, 12f, 0f, paint);
        Bitmap flippedV = modifyBitmap(src, FLIP_VERTICAL);
        canvas.drawBitmap(flippedV, 0f, 12f, paint);
        Bitmap flippedBoth = modifyBitmap(src, FLIP_BOTH);
        canvas.drawBitmap(flippedBoth, 12f, 12f, paint);

        return result;
    }
}
