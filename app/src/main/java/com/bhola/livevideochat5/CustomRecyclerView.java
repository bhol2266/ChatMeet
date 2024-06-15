package com.bhola.livevideochat5;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerView extends RecyclerView {

    private float startX;
    private float startY;
    private boolean isHorizontalScrolling;

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = e.getX();
                startY = e.getY();
                isHorizontalScrolling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = e.getX() - startX;
                float dy = e.getY() - startY;
                if (Math.abs(dx) > Math.abs(dy)) {
                    isHorizontalScrolling = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                isHorizontalScrolling = false;
                break;
        }
        return super.onInterceptTouchEvent(e) && isHorizontalScrolling;
    }
}
