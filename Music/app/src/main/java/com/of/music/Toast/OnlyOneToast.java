package com.of.music.Toast;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by MR.XIE on 2018/8/15.
 */
public class OnlyOneToast {
    private static Toast toast;
    private String TAG="Music";
    public static void  makeText(Context context, String content){
        if(toast==null){
        toast=Toast.makeText(context,content,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        }else{
            toast.cancel();
            toast=Toast.makeText(context,content,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
        }
        toast.show();
    }
}
