package com.phonar.map;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.phonar.DevicesList;
import com.phonar.models.Device;

/**
 * Overlay list required by PhonarMapActivity
 */
public class PhonarItemizedOverlay extends ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    private Context mContext;

    /**
     * constructor
     * 
     * @param defaultMarker
     *            Marker to draw on map
     * @param context
     *            Application context
     */
    public PhonarItemizedOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
    }

    /**
     * Add overlay to list
     * 
     * @param overlay
     *            Overlay to add
     */
    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

    /**
     * Override for populate() to work
     */
    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }

    /**
     * Remove all overlays
     */
    public void clear() {
        mOverlays.clear();
        populate();
    }

    /**
     * Number of overlays
     */
    @Override
    public int size() {
        return mOverlays.size();
    }

    /**
     * What will happen when overlayItem is tapped in map view
     */
    @Override
    protected boolean onTap(int index) {
        OverlayItem item = mOverlays.get(index);
        if (item.getTitle().isEmpty()) {
            Device device = DevicesList.get(mContext, item.getSnippet());
            if (device != null) {
                device.showOptionsDialog(mContext);
            }
        }
        return true;
    }

    // Only draw the item if it is not a shadow.
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (!shadow) {
            super.draw(canvas, mapView, false);
        }
    }
}
