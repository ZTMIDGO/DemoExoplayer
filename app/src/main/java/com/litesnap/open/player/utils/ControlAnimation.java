package com.litesnap.open.player.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ControlAnimation {
    private List<AnimatorSet> mList;

    public ControlAnimation(){
        mList = new ArrayList<>();
    }

    public void startAnimation(View view, Drawable drawable, boolean isVisible){
        for (AnimatorSet animator : mList){
            animator.cancel();
        }

        float viewEnd = isVisible ? 1.0f : 0f;
        int drawEnd = isVisible ? 255 : 0;

        AnimatorSet set = new AnimatorSet();

        ObjectAnimator viewAni = ObjectAnimator.ofFloat(view, "Alpha", view.getAlpha(), viewEnd);
        ObjectAnimator drawAni = ObjectAnimator.ofInt(drawable, "Alpha", drawable.getAlpha(), drawEnd);
        set.playSequentially(viewAni, drawAni);

        mList.add(set);

        set.setDuration(300);
        set.start();
    }
}
