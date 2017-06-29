package com.dm.wallpaper.board.utils;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.widget.TextView;

import com.dm.wallpaper.board.R;

public class TextViewPadding {

    public static void setPaddings(TextView textView, Activity activity) {
        int colour = activity.getResources().getColor(R.color.toolbarIcon);
        textView.setTextColor(colour);
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        TypedArray typedArray = activity.obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        int actionSize = typedArray.getDimensionPixelSize(0, 0);
        typedArray.recycle();
        textView.setPadding(30, size.y / 2 - actionSize * 3, 30, 30);
    }
}
