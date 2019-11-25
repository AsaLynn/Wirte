package com.zxn.library.blackboxprogress.utils;

import android.content.Context;

/**
 * Created by think on 2018/1/16.
 */

public class UIUtils {

    public static int dp2px(Context context, int dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
