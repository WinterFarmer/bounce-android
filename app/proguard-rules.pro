# Aturan dasar Android
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses

# Amankan Activity utama dan GameView agar tidak di-strip oleh AAPT
-keep public class com.android.bounce.MainActivity {
    public *;
}

-keep public class com.android.bounce.GameView {
    public *;
}
