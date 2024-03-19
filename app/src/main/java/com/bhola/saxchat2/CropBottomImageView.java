package com.bhola.saxchat2;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class CropBottomImageView extends androidx.appcompat.widget.AppCompatImageView {

    public CropBottomImageView(Context context) {
        super(context);
    }

    public CropBottomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CropBottomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();

        if (drawable == null) {
            super.onDraw(canvas);
            return;
        }

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();

        float scale;
        int xOffset = 0;
        int yOffset = 0;

        if (drawableWidth * viewHeight > viewWidth * drawableHeight) {
            // Crop from bottom
            scale = (float) viewHeight / (float) drawableHeight;
            xOffset = (int) ((viewWidth - drawableWidth * scale) / 2);
        } else {
            // Scale to fit width
            scale = (float) viewWidth / (float) drawableWidth;
            yOffset = (int) ((viewHeight - drawableHeight * scale) / 2);
        }

        canvas.save();
        canvas.scale(scale, scale);
        canvas.translate(xOffset / scale, yOffset / scale);
        drawable.draw(canvas);
        canvas.restore();
    }
}
