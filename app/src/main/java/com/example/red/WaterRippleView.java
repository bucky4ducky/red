package com.example.red;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.core.content.ContextCompat;

public class WaterRippleView extends View {

    private Paint ripplePaint;
    private ValueAnimator animator;
    private float rippleRadius = 0f;
    private float rippleAlpha = 255;
    private int rippleBaseColor;
    private int rippleHighlightColor;

    private static final long ANIMATION_DURATION = 2000; // 2 seconds for one cycle
    private static final int MAX_RIPPLE_ALPHA = 255;
    private static final float STROKE_WIDTH_DP = 4f; // Stroke width in DP

    public WaterRippleView(Context context) {
        super(context);
        init(null);
    }

    public WaterRippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public WaterRippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setStrokeWidth(dpToPx(STROKE_WIDTH_DP)); // Convert DP to pixels


        rippleBaseColor = ContextCompat.getColor(getContext(), R.color.ripple_color);
        rippleHighlightColor = ContextCompat.getColor(getContext(), R.color.ripple_highlight);

        startRippleAnimation();
    }


    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void startRippleAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(ANIMATION_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE); // Loop infinitely
        animator.setInterpolator(new LinearInterpolator()); // Consistent speed

        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();


            rippleRadius = animatedValue * Math.max(getWidth(), getHeight()) / 2f;

            if (animatedValue < 0.5f) {
                rippleAlpha = MAX_RIPPLE_ALPHA;
            } else {
                rippleAlpha = MAX_RIPPLE_ALPHA * (1f - (animatedValue - 0.5f) * 2f); // Fades from 0.5 to 1.0
            }

            // Adjust color slightly based on animation progress for a flowing effect
            int color;
            if (animatedValue < 0.5f) {
                color = interpolateColor(rippleBaseColor, rippleHighlightColor, animatedValue * 2f);
            } else {
                color = interpolateColor(rippleHighlightColor, rippleBaseColor, (animatedValue - 0.5f) * 2f);
            }

            ripplePaint.setColor(color);
            ripplePaint.setAlpha((int) rippleAlpha);

            invalidate(); // Redraw the view
        });

        animator.start();
    }

    // Simple linear color interpolation
    private int interpolateColor(int color1, int color2, float fraction) {
        int a = (int) (Color.alpha(color1) * (1 - fraction) + Color.alpha(color2) * fraction);
        int r = (int) (Color.red(color1) * (1 - fraction) + Color.red(color2) * fraction);
        int g = (int) (Color.green(color1) * (1 - fraction) + Color.green(color2) * fraction);
        int b = (int) (Color.blue(color1) * (1 - fraction) + Color.blue(color2) * fraction);
        return Color.argb(a, r, g, b);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Restart animation if size changes to ensure ripple scales correctly
        startRippleAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        if (rippleRadius > 0) {
            canvas.drawCircle(centerX, centerY, rippleRadius, ripplePaint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animator == null) {
            startRippleAnimation();
        } else if (!animator.isRunning()) {
            animator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel(); // Stop animation to prevent leaks
            animator = null;
        }
    }
}