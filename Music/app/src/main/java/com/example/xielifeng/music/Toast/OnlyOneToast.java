package com.example.xielifeng.music.Toast;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by MR.XIE on 2018/8/15.
 */
public class OnlyOneToast {
    private static Toast toast;
    public static void  makeText(Context context, String content){
        if(toast==null){
        toast=Toast.makeText(context,content,Toast.LENGTH_SHORT);
        }else{
            toast.setText(content);
        }
        toast.show();
    }
}
