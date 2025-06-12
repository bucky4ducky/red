package com.example.red;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.lang.ref.WeakReference;

public class MenuIconTarget implements Target {
    private final WeakReference<MenuItem> menuItemRef;
    private final WeakReference<Context> contextRef;

    public MenuIconTarget(Context context, MenuItem menuItem) {
        this.menuItemRef = new WeakReference<>(menuItem);
        this.contextRef = new WeakReference<>(context);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        MenuItem item = menuItemRef.get();
        Context context = contextRef.get();
        if (item != null && context != null) {
            item.setIcon(new BitmapDrawable(context.getResources(), bitmap));
        }
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        MenuItem item = menuItemRef.get();
        if (item != null) {
            item.setIcon(errorDrawable);
        }
    }

    @Override
    public void onPrepareLoad(@Nullable Drawable placeHolderDrawable) {
        MenuItem item = menuItemRef.get();
        if (item != null && placeHolderDrawable != null) {
            item.setIcon(placeHolderDrawable);
        }
    }
}