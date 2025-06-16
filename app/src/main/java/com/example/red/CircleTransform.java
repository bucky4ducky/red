package com.example.red;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;



public class CircleTransform implements Transformation {
    private final int borderWidth;
    private final int borderColor;

    // Constructor without border
    public CircleTransform() {
        this(0, 0);
    }

    // Constructor with border
    public CircleTransform(int borderWidth, int borderColor) {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        // Calculate center crop coordinates
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        // Create squared bitmap
        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        // Create output bitmap
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // Create paint for the circular image
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap,
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        // Draw the circular image
        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        // Draw border if needed
        if (borderWidth > 0) {
            Paint borderPaint = new Paint();
            borderPaint.setColor(borderColor);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(borderWidth);
            borderPaint.setAntiAlias(true);
            canvas.drawCircle(radius, radius, radius - borderWidth/2f, borderPaint);
        }

        squaredBitmap.recycle();
        return output;
    }

    @Override
    public String key() {
        return "circle" + (borderWidth > 0 ? "_border_" + borderWidth : "");
    }
}