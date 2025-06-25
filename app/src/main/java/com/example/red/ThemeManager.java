package com.example.red;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ThemeManager {

    public static void apply(Context context, View rootView) {
        ConfigData config = storage.getInstance().getConfigData();
        if (config == null) return;

        try {
            int themeColor = Color.parseColor(config.getThemeColor());
            int textColor = Color.parseColor(config.getTextColor());
            int backgroundColor = Color.parseColor(config.getAppBackgroundColor());
            int menuColor = Color.parseColor(config.getMenuBackgroundColor());
            int gradientColor = Color.parseColor(config.getThemeGradiantColor());

            if ("dark".equalsIgnoreCase(config.getTheme())) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            if (context instanceof Activity) {
                Window window = ((Activity) context).getWindow();
                window.setStatusBarColor(themeColor);
                window.setNavigationBarColor(menuColor);
            }

            if (config.getGradiant() != null && config.getGradiant()) {
                GradientDrawable gradient = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{themeColor, gradientColor}
                );
                rootView.setBackground(gradient);
            } else {
                rootView.setBackgroundColor(backgroundColor);
            }

            applyToViews(rootView, themeColor, textColor, backgroundColor);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyToViews(View view, int themeColor, int textColor, int cardColor) {
        if (view instanceof MaterialToolbar) {
            view.setBackgroundColor(themeColor);
        } else if (view instanceof FloatingActionButton) {
            ((FloatingActionButton) view).setBackgroundTintList(android.content.res.ColorStateList.valueOf(themeColor));
        } else if (view instanceof Button) {
            view.setBackgroundTintList(android.content.res.ColorStateList.valueOf(themeColor));
            ((Button) view).setTextColor(textColor);
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(textColor);
        } else if (view instanceof CardView) {
            ((CardView) view).setCardBackgroundColor(cardColor);
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyToViews(vg.getChildAt(i), themeColor, textColor, cardColor);
            }
        }
    }
}

