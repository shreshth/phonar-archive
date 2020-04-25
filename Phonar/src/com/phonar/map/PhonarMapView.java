package com.phonar.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.google.android.maps.MapView;
import com.phonar.PhonarTabActivity;
import com.phonar.R;

/**
 * Slightly tweaked version of MapView that allows for zoom on double-click
 */
public class PhonarMapView extends MapView {
	private long lastTouchTime = -1;

	public PhonarMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Double-click event handler
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			long thisTime = System.currentTimeMillis();
			if ((thisTime - lastTouchTime) < 250) {
				// Double tap
				this.getController().zoomInFixing((int) ev.getX(), (int) ev.getY());
				lastTouchTime = -1;
			} else {
				lastTouchTime = thisTime;
			}
		}

		// hide menu
		LinearLayout addMenu = (LinearLayout) PhonarTabActivity.mMap
						.findViewById(R.id.add_button_menu_map);
		if (addMenu.getVisibility() == View.VISIBLE) {
			addMenu.setAnimation(AnimationUtils.loadAnimation(PhonarTabActivity.mMap,
							R.anim.menu_fadeout));
			addMenu.setVisibility(View.GONE);
		}

		return super.onInterceptTouchEvent(ev);
	}
}