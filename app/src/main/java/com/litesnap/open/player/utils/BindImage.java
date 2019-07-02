package com.litesnap.open.player.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

public class BindImage {
    public static void bindImage(ImageView imageView, int resId, Context context){
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(200).setCrossFadeEnabled(true).build();
        Glide.with(context)
                .load(resId)
                .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                .into(imageView);
    }

    public static void bindImage(ImageView imageView, String url, Context context){
        DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(200).setCrossFadeEnabled(true).build();
        Glide.with(context)
                .load(url)
                .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                .into(imageView);
    }
}
