package com.android.bounce.core;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager implements SoundListener {
    private SoundPool soundPool;
    private int soundPopId = -1;
    private int soundPickupId = -1;
    private int soundUpId = -1;

    public SoundManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attrs)
                .build();

        loadSounds(context);
    }

    private void loadSounds(Context context) {
        soundPopId = loadAudio(context, "pop");
        soundPickupId = loadAudio(context, "pickup");
        soundUpId = loadAudio(context, "up");
    }

    private int loadAudio(Context context, String baseName) {
        String[] paths = {
            "sounds/" + baseName + ".ogg",
            "sound/" + baseName + ".ogg",
            "sounds/" + baseName + ".wav",
            "sound/" + baseName + ".wav",
            baseName + ".ogg"
        };

        for (String path : paths) {
            try {
                AssetFileDescriptor afd = context.getAssets().openFd(path);
                int id = soundPool.load(afd, 1);
                afd.close();
                return id;
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

    @Override
    public void playPop() {
        if (soundPool != null && soundPopId != -1) {
            soundPool.play(soundPopId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    @Override
    public void playPickup() {
        if (soundPool != null && soundPickupId != -1) {
            soundPool.play(soundPickupId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    @Override
    public void playUp() {
        if (soundPool != null && soundUpId != -1) {
            soundPool.play(soundUpId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
