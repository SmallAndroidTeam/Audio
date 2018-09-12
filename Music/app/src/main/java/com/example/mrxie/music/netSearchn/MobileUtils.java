package com.example.mrxie.music.netSearchn;//package com.ofilm.test.viewpager1.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

// * 2015年8月15日 16:34:37
// * 博文地址：http://blog.csdn.net/u010156024
// */
public class MobileUtils {
	/**

	 */
	public static void hideInputMethod(View view) {
		InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
