package com.cjdell.podclient.helpers;

import android.content.Context;

import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.IconicIcon;

/**
 * Created by cjdell on 23/01/14.
 */
public class TextHelper {

    public static String getTimeString(int msec) {
        String min = "" + (msec / 1000) / 60;
        String sec = "" +  (msec / 1000) % 60;

        if (sec.length() == 1) sec = "0" + sec;

        return min + ":" + sec;
    }

    public static IconicFontDrawable getIcon(Context context, IconicIcon icon, int color) {
        IconicFontDrawable iconicFontDrawable = new IconicFontDrawable(context);
        iconicFontDrawable.setIcon(icon);
        iconicFontDrawable.setIconColor(color);
        return iconicFontDrawable;
    }

}
