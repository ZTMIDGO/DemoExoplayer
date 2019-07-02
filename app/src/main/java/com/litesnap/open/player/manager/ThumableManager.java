package com.litesnap.open.player.manager;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class ThumableManager {
    private static Drawable mDrawable;

    public static void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    public static Drawable getDrawable() {
        return mDrawable;
    }
}
