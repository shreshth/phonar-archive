package com.phonar.ar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phonar.DevicesList;
import com.phonar.PhonarTabActivity;
import com.phonar.R;
import com.phonar.models.Device;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.ImageUtils;
import com.phonar.utils.log;

public class DevicesLayer implements Camera.PreviewCallback {
    private Context mContext = null;
    private Map<String, DeviceLayerLayout> mDevices = new HashMap<String, DeviceLayerLayout>();
    private Map<String, DeviceLayerLayout> mOffscreenDevices =
        new HashMap<String, DeviceLayerLayout>();
    private boolean init = false;
    private Camera mCamera = null;
    // private Location you;
    float hva;
    float vva;
    int[] dims;
    int width;
    int height;
    private boolean initialized = false;
    Matrix mat = new Matrix();
    private boolean hasLocation = false;

    private Bitmap offscreenBitmap;

    /**
     * Constructor
     */
    public DevicesLayer(Context context) {
        mContext = context;
        init = false;
        try {
            int attempt = 0;
            while (attempt < 3 && mCamera == null) {
                mCamera = Camera.open();
                attempt = attempt + 1;
            }
        } catch (RuntimeException e) {
            log.e("Camera error", "Error connecting camera");
        }
        if (mCamera == null) {
            log.e("Camera error", "No backward-facing camera found");
        }

        offscreenBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrow);
    }

    public Camera getCamera() {
        return mCamera;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
        }
    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1) {
        if (hasLocation) {
            // Don't do this init in constructor because ImageViews need to be
            // initialized after camera layer
            // This will initialize the ImageViews and add them to a list to be
            // accessed later
            if (!init) {
                List<Device> deviceList = DevicesList.getAll(mContext);
                if (deviceList != null) {
                    for (final Device device : deviceList) {
                        if (device != null
                            && device.timeOfLastLocation != null
                            && System.currentTimeMillis() - device.timeOfLastLocation < PhonarTabActivity.MAX_AGE) {
                            addDevices(mDevices, mOffscreenDevices, device.phoneNumber, ImageUtils
                                .getPaddedBitmap(device.image));
                        }
                    }
                }
                init = true;
                adjustDistances();
            } else {
                for (Entry<String, DeviceLayerLayout> deviceEntry : mDevices.entrySet()) {
                    Device device = DevicesList.get(mContext, deviceEntry.getKey());
                    DeviceLayerLayout layout = deviceEntry.getValue();
                    DeviceLayerLayout offScreenLayout = mOffscreenDevices.get(device.phoneNumber);
                    if (device.bearingTo != null) {
                        adjustDevices(
                            getDeviceCoordinates(device.bearingTo), layout, offScreenLayout,
                            layout.bitmap, device.timeOfLastLocation);
                    } else {
                        layout.setVisibility(View.GONE);
                        offScreenLayout.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void adjustDevices(
        Coordinates c, DeviceLayerLayout layout, DeviceLayerLayout offScreenLayout, Bitmap bMap,
        Long lastTime) {
        if (c == null) {
            return;
        }

        dims = ((ARActivity) mContext).getCLayer().getCameraDims();
        width = dims[0];
        height = dims[1];
        boolean isOnScreen = true;

        int xcoord = c.x;
        if (c.x == OrientationLogic.OUT_OF_BOUNDS_NEGATIVE) {
            xcoord = 0;
            isOnScreen = false;
        } else if (c.x == OrientationLogic.OUT_OF_BOUNDS_POSITIVE) {
            xcoord = width;
            isOnScreen = false;
        }

        int ycoord = c.y;
        if (c.y == OrientationLogic.OUT_OF_BOUNDS_NEGATIVE) {
            ycoord = 0;
            isOnScreen = false;
        } else if (c.y == OrientationLogic.OUT_OF_BOUNDS_POSITIVE) {
            ycoord = height;
            isOnScreen = false;
        }

        if (isOnScreen) {
            layout.setVisibility(View.VISIBLE);
            offScreenLayout.setVisibility(View.GONE);

            ImageView image = (ImageView) ((RelativeLayout) layout.getChildAt(0)).getChildAt(0);

            if (xcoord > width - bMap.getWidth()) {
                xcoord = width - bMap.getWidth();
            }

            if (ycoord > height - bMap.getHeight()) {
                ycoord = height - bMap.getHeight();
            }

            float angle = (float) c.theta;

            layout.setAngle(angle);

            // grey out image if more than one hour
            long curTime = System.currentTimeMillis();
            if (curTime - lastTime > PhonarTabActivity.STALE_AGE) {
                Bitmap newBMap = ImageUtils.toGrayscale(bMap);
                image.setImageBitmap(newBMap);
            } else {
                image.setImageBitmap(bMap);
            }

            // Compute distance to device and put text view
            TextView text = (TextView) ((RelativeLayout) layout.getChildAt(0)).getChildAt(1);
            if (isOnScreen) {
                text.setVisibility(View.VISIBLE);
            } else {
                text.setVisibility(View.GONE);
            }

            DeviceLayerLayout.LayoutParams p =
                (DeviceLayerLayout.LayoutParams) layout.getLayoutParams();
            p.setMargins(xcoord - layout.getWidth() / 2, ycoord, 0, 0);
            layout.setLayoutParams(p);

        } else {
            layout.setVisibility(View.GONE);
            offScreenLayout.setVisibility(View.VISIBLE);

            if (xcoord > width - bMap.getWidth() - bMap.getWidth()) {
                xcoord = width - bMap.getWidth() - bMap.getWidth();
            }

            if (ycoord > height - bMap.getHeight() - bMap.getHeight()) {
                ycoord = height - bMap.getHeight() - bMap.getHeight();
            }

            // arrow image
            ImageView image =
                (ImageView) ((RelativeLayout) offScreenLayout.getChildAt(0)).getChildAt(0);
            RelativeLayout.LayoutParams arrowparams =
                new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            // device image
            ImageView sideImage =
                (ImageView) ((RelativeLayout) offScreenLayout.getChildAt(0)).getChildAt(1);
            RelativeLayout.LayoutParams deviceparams =
                new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            float angle = 0f;
            if (c.x == OrientationLogic.OUT_OF_BOUNDS_NEGATIVE) {
                angle = -90f;
                deviceparams.addRule(RelativeLayout.RIGHT_OF, image.getId());
                if (c.y == OrientationLogic.OUT_OF_BOUNDS_NEGATIVE) {
                    angle = -45f;
                    deviceparams.addRule(RelativeLayout.BELOW, image.getId());
                } else if (c.y == OrientationLogic.OUT_OF_BOUNDS_POSITIVE) {
                    angle = -135f;
                    arrowparams.addRule(RelativeLayout.BELOW, sideImage.getId());
                }
            } else if (c.x == OrientationLogic.OUT_OF_BOUNDS_POSITIVE) {
                angle = 90f;
                arrowparams.addRule(RelativeLayout.RIGHT_OF, sideImage.getId());
                if (c.y == OrientationLogic.OUT_OF_BOUNDS_NEGATIVE) {
                    angle = 45f;
                    deviceparams.addRule(RelativeLayout.BELOW, image.getId());
                } else if (c.y == OrientationLogic.OUT_OF_BOUNDS_POSITIVE) {
                    angle = 135f;
                    arrowparams.addRule(RelativeLayout.BELOW, sideImage.getId());
                }
            } else if (c.y == OrientationLogic.OUT_OF_BOUNDS_NEGATIVE) {
                angle = 0f;
                deviceparams.addRule(RelativeLayout.BELOW, image.getId());
            } else if (c.y == OrientationLogic.OUT_OF_BOUNDS_POSITIVE) {
                angle = 180f;
                arrowparams.addRule(RelativeLayout.BELOW, sideImage.getId());
            }

            mat.setRotate(angle);
            Bitmap bMapRotate =
                Bitmap.createBitmap(
                    offscreenBitmap, 0, 0, offscreenBitmap.getWidth(), offscreenBitmap.getHeight(),
                    mat, true);
            image.setImageBitmap(bMapRotate);
            image.setLayoutParams(arrowparams);

            float deviceImageAngle = (float) c.theta;
            mat.setRotate(deviceImageAngle);
            Bitmap bMapSide = bMap;
            Bitmap bMapSideRotate =
                Bitmap.createBitmap(
                    bMapSide, 0, 0, bMapSide.getWidth(), bMapSide.getHeight(), mat, true);
            sideImage.setLayoutParams(deviceparams);
            // grey out image if more than one hour
            long curTime = System.currentTimeMillis();
            if (curTime - lastTime > PhonarTabActivity.STALE_AGE) {
                Bitmap newBMap = ImageUtils.toGrayscale(bMapSideRotate);
                sideImage.setImageBitmap(newBMap);
            } else {
                sideImage.setImageBitmap(bMapSideRotate);
            }

            DeviceLayerLayout.LayoutParams p =
                (DeviceLayerLayout.LayoutParams) offScreenLayout.getLayoutParams();
            p.setMargins(xcoord, ycoord, 0, 0);
            offScreenLayout.setLayoutParams(p);
        }
    }

    /**
     * Add device to AR. NOTE PARAMS BELOW.
     * 
     * @param phoneNumber
     *            For devices, this is the device phone number. For friends,
     *            this is the lookup key.
     */
    private void addDevices(
        Map<String, DeviceLayerLayout> map, Map<String, DeviceLayerLayout> offscreenmap,
        final String phoneNumber, Bitmap bitmap) {
        LayoutInflater inflater =
            (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DeviceLayerLayout layout = (DeviceLayerLayout) inflater.inflate(R.layout.device_ar, null);
        layout.bitmap = bitmap;

        ImageView image = (ImageView) ((RelativeLayout) layout.getChildAt(0)).getChildAt(0);
        image.setImageBitmap(bitmap);
        // add to view and device list
        ((RelativeLayout) ((Activity) mContext).findViewById(R.id.ar)).addView(layout);
        map.put(phoneNumber, layout);

        // do the same for the offscreen layout
        DeviceLayerLayout offscreenLayout =
            (DeviceLayerLayout) inflater.inflate(R.layout.device_ar_offscreen, null);
        offscreenLayout.bitmap = bitmap;
        ImageView offScreenImage =
            (ImageView) ((RelativeLayout) offscreenLayout.getChildAt(0)).getChildAt(0);
        offScreenImage.setImageBitmap(offscreenBitmap);
        ImageView sideOffScreenImage =
            (ImageView) ((RelativeLayout) offscreenLayout.getChildAt(0)).getChildAt(1);
        sideOffScreenImage.setImageBitmap(bitmap);
        sideOffScreenImage.setAlpha(150);

        ((RelativeLayout) ((Activity) mContext).findViewById(R.id.ar)).addView(offscreenLayout);

        offscreenmap.put(phoneNumber, offscreenLayout);

        // Set click listeners
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Device device = DevicesList.get(mContext, phoneNumber);
                if (device != null) {
                    device.showOptionsDialog(mContext);
                }
            }
        });
        offscreenLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Device device = DevicesList.get(mContext, phoneNumber);
                if (device != null) {
                    device.showOptionsDialog(mContext);
                }
            }
        });
    }

    public void gotLocation() {
        hasLocation = true;
    }

    private Coordinates getDeviceCoordinates(Float bearingTo) {
        float[] orientations = ((ARActivity) mContext).getOrientations();
        if (mContext != null && ((ARActivity) mContext).getCLayer() != null && initialized == false) {
            dims = ((ARActivity) mContext).getCLayer().getCameraDims();
            hva = (float) Math.toRadians(mCamera.getParameters().getHorizontalViewAngle());
            vva = (float) Math.toRadians(mCamera.getParameters().getVerticalViewAngle());
            width = dims[0];
            height = dims[1];
            initialized = true;
        }

        Coordinates c =
            OrientationLogic
                .getOverlayCoordinates(bearingTo, orientations, hva, vva, width, height);
        return c;
    }

    public void adjustDistances() {
        for (Entry<String, DeviceLayerLayout> deviceEntry : mDevices.entrySet()) {
            Device device = DevicesList.get(mContext, deviceEntry.getKey());
            if (device.distanceTo != null) {
                float distance = device.distanceTo;
                DeviceLayerLayout layout = deviceEntry.getValue();
                TextView text = (TextView) ((RelativeLayout) layout.getChildAt(0)).getChildAt(1);
                text.setText(CommonUtils.getFormattedDistance(mContext, distance));
            }
        }
    }
}