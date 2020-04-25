package com.phonar.ar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class DeviceLayerLayout extends RelativeLayout {
    private float angle = 0;
    public Bitmap bitmap;

    public DeviceLayerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DeviceLayerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DeviceLayerLayout(Context context) {
        super(context);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        RelativeLayout layout = (RelativeLayout) this.getChildAt(0);
        canvas.rotate(angle, layout.getWidth() / 2, layout.getHeight() / 2);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public void setAngle(float angle) {
        if (angle <= 180 && angle >= -180) {
            this.angle = angle;
        }
    }
}
