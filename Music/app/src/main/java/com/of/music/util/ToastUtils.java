package com.of.music.util;

import android.content.Context;
import android.widget.Toast;

import com.of.music.Application.App;

/**
 * Toast工具类
 * Created by wcy on 2015/12/26.
 */
public class ToastUtils {
    private static Context sContext;
    private static Toast sToast;

    public static void init(Context context) {
        sContext = App.sContext;
    }

    public static void show(int resId) {
        show(sContext.getString(resId));
    }

    public static void show(String text) {
        if (sToast == null) {
            sToast = Toast.makeText(App.sContext, text, Toast.LENGTH_SHORT);
        } else {
            sToast.setText(text);
        }
        sToast.show();
    }
}
