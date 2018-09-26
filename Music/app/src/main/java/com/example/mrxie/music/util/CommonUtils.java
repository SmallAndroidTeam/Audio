package com.example.mrxie.music.util;

import android.os.Build;

public class CommonUtils {

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
