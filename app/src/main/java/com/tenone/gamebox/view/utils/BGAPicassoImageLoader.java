package com.tenone.gamebox.view.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class BGAPicassoImageLoader extends BGAImageLoader {

    @Override
    public void displayImage(Activity activity, final ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, final DisplayDelegate delegate) {
        final String finalPath = getPath(path);
        Picasso.with(activity).load(finalPath).placeholder(loadingResId).error(failResId).resize(width, height).centerInside().into(imageView, new Callback.EmptyCallback() {
            @Override
            public void onSuccess() {
                if (delegate != null) {
                    delegate.onSuccess(imageView, finalPath);
                }
            }
        });
    }

    @Override
    public void downloadImage(Context context, String path, final DownloadDelegate delegate) {
        final String finalPath = getPath(path);
        Picasso.with(context.getApplicationContext()).load(finalPath).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (delegate != null) {
                    delegate.onSuccess(finalPath, bitmap);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (delegate != null) {
                    delegate.onFailed(finalPath);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }

}