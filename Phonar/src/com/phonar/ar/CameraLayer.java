package com.phonar.ar;

import java.io.IOException;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.phonar.utils.log;

/**
 * Camera view that is visible in the main ARActivity
 */
public class CameraLayer extends SurfaceView implements SurfaceHolder.Callback, PreviewCallback {

    private Camera mCamera = null;
    private boolean isRunning = false;
    private DevicesLayer mDeviceLayerCallback = null;

    // variable to track size of the CameraView
    private int[] cameraDims;

    /**
     * Constructor
     */
    @SuppressWarnings("deprecation")
    public CameraLayer(Context context, DevicesLayer callback) {
        super(context);
        mDeviceLayerCallback = callback;

        // create surface, install callback for when surface is created (see
        // surfaceCreated() below)
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraDims = new int[2];

    }

    /**
     * When surface is created, add camera preview on it
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isRunning) {
            return;
        }

        synchronized (this) {
            // open camera
            mCamera = mDeviceLayerCallback.getCamera();

            // start preview
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                log.e("Camera error", "Setting preview display on surface failed");
            }

            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
            isRunning = true;

            // Record the camera view width/height
            cameraDims[0] = this.getWidth();
            cameraDims[1] = this.getHeight();
        }
    }

    /**
     * Surface will be destroyed when we return, so stop the preview. Because
     * the camera object is not a shared resource, it's very important to
     * release it when the activity is paused (otherwise we'll lock it up)
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!isRunning) {
            return;
        }

        synchronized (this) {
            try {
                if (mCamera != null) {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.release();
                }
            } catch (Exception e) {
                log.e("Camera error", e.getMessage());
            }
            isRunning = false;
        }
    }

    /**
     * When surface is changed
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Nothing to do here
    }

    /**
     * Every time we get a new camera image, call the GL layer's previewFrame()
     * function
     */
    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1) {
        if (mDeviceLayerCallback != null) {
            mDeviceLayerCallback.onPreviewFrame(arg0, arg1);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    // @Override
    // protected void onLayout (boolean changed, int left, int top, right,
    // bottom) {
    //
    // }

    public int[] getCameraDims() {
        return cameraDims;
    }
}
