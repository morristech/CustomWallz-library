package com.dm.wallpaper.board.utils;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.widget.TextView;

import com.dm.wallpaper.board.R;

public class TextViewPadding {

    public void setPaddings(TextView textView, Activity activity, boolean reducePadding) {
        int colour = activity.getResources().getColor(R.color.toolbarIcon);
        textView.setTextColor(colour);
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        TypedArray typedArray = activity.obtainStyledAttributes(new int[]{R.attr.actionBarSize});
        int actionSize = typedArray.getDimensionPixelSize(0, 0);
        typedArray.recycle();
        int multiplier = reducePadding ? 1 : 3;
        textView.setPadding(30, size.y / 2 - actionSize * multiplier, 30, 30);
    }
}
