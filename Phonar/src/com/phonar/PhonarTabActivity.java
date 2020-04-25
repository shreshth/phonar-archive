package com.phonar;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.phonar.ar.ARActivity;
import com.phonar.map.PhonarMapActivity;
import com.phonar.utils.GeoUtils;

/**
 * 
 * Parent activity of the three tabs
 * 
 */
@SuppressWarnings("deprecation")
public class PhonarTabActivity extends TabActivity implements OnGestureListener {
	private static String WAKE_LOCK = "screen_wake_lock";
	private static PowerManager.WakeLock wakelock;
	public static PhonarTabActivity mPhonarTabActivity;
	public static final String KEY_SHOW_MAP = "show map?";
	public static boolean mIsRunning = false;
	public static boolean mIsARRunning = false;
	public static boolean mIsMapRunning = false;
	public static ARActivity mAR = null;
	public static PhonarMapActivity mMap = null;

	private static final String TAB_ID_AR = "ar";
	private static final String TAB_ID_MAP = "map";
	private static final String TAB_ID_REQUEST = "request";
	public static final int TAB_REQUEST = -1;
	public static final int TAB_MAP = 1;
	public static final int TAB_AR = 0;
	private static final int NUM_TABS = 3;

	// the device will not be displayed in ar/maps if the last known location is
	// older than this in milliseconds
	public static long MAX_AGE = 6 * 60 * 60 * 1000;
	public static long STALE_AGE = 60 * 60 * 1000;

	// swipey stuff
	private GestureDetector gestureScanner;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final int ANIMATION_DURATION = 3000;
	// maximum slope for swipe gesture
	private static final double MAX_SLOPE = Math.tan(Math.toRadians(30));

	public static int mCurrentChild;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gestureScanner = new GestureDetector(this);
		mPhonarTabActivity = this;
		overridePendingTransition(R.anim.splash_fadein, R.anim.do_nothing);
		setContentView(R.layout.main);

		// Fetch list of devices
		if (DevicesList.getAll(this) == null || DevicesList.getAll(this).isEmpty()) {
			DevicesList.refreshAll(this);
		}

		// Fetch Location once
		GeoUtils.singleLocationUpdate(this);

		// Set up Tabs
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		// ARActivity
		intent = new Intent().setClass(this, ARActivity.class);
		spec = tabHost.newTabSpec(TAB_ID_AR).setIndicator(getString(R.string.tab_ar))
						.setContent(intent);
		tabHost.addTab(spec);

		// MapActivity
		intent = new Intent().setClass(this, PhonarMapActivity.class);
		spec = tabHost.newTabSpec(TAB_ID_MAP).setIndicator(getString(R.string.tab_map))
						.setContent(intent);
		tabHost.addTab(spec);

		// Initial tab selection
		mCurrentChild = TAB_AR;
		tabHost.setCurrentTab(TAB_AR);
		tabHost.setOnTabChangedListener(tabChangeListener);

		// style tabs
		Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Bold.ttf");
		for (int i = 0; i < getTabWidget().getChildCount(); i++) {
			View tab = getTabWidget().getChildAt(i);
			tab.setBackgroundResource(R.drawable.tab);
			tab.getLayoutParams().height = (int) (50f * getResources().getDisplayMetrics().density);
			TextView tv = (TextView) tab.findViewById(android.R.id.title);
			tv.setTextColor(getResources().getColor(R.color.pink));
			tv.setTypeface(tf);
			tv.setGravity(Gravity.CENTER);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			tabHost.getTabWidget().setDividerDrawable(R.color.gray_divider);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (this.getIntent().getBooleanExtra(KEY_SHOW_MAP, false)) {
			// Set up Tabs
			mCurrentChild = TAB_MAP;
			TabHost tabHost = getTabHost();
			tabHost.setCurrentTab(TAB_MAP);
		}
	}

	// prevent sleep
	public static void disableSleep(Context context) {
		// Prevent sleep
		if (wakelock == null) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			wakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, WAKE_LOCK);
			wakelock.acquire();
		}
	}

	// reenable sleep
	public static void enableSleep(Context context) {
		// Enable sleep
		if (wakelock != null) {
			wakelock.release();
			wakelock = null;
		}
	}

	/*
	 * Swipey stuff
	 */

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (gestureScanner != null) {
			if (gestureScanner.onTouchEvent(ev)) {
				return true;
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// ignore maps
		if (mCurrentChild == TAB_MAP) {
			return false;
		}

		// Check movement along the Y-axis. If too much, then ignore
		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH
						|| Math.abs(e1.getY() - e2.getY()) > MAX_SLOPE
										* Math.abs(e1.getX() - e2.getX())) {
			return false;
		}

		// Swipe from right to left (if certain distance and velocity)
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			if (mCurrentChild != NUM_TABS - 1) {
				getTabHost().setCurrentTab(mCurrentChild + 1);
			}
			return true;
		}

		// Swipe from left to right (if certain distance and velocity)
		if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			if (mCurrentChild != 0) {
				getTabHost().setCurrentTab(mCurrentChild - 1);
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@SuppressWarnings("unused")
	private static Animation inFromRightAnimation() {
		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(ANIMATION_DURATION);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	@SuppressWarnings("unused")
	private static Animation outToLeftAnimation() {
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(ANIMATION_DURATION);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	@SuppressWarnings("unused")
	private static Animation inFromLeftAnimation() {
		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(ANIMATION_DURATION);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	@SuppressWarnings("unused")
	private static Animation outToRightAnimation() {
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
						Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(ANIMATION_DURATION);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	public OnTabChangeListener tabChangeListener = new OnTabChangeListener() {
		@Override
		public void onTabChanged(String tabID) {
			// update current view
			if (tabID == TAB_ID_AR) {
				mCurrentChild = TAB_AR;
			} else if (tabID == TAB_ID_MAP) {
				mCurrentChild = TAB_MAP;
			} else if (tabID == TAB_ID_REQUEST) {
				mCurrentChild = TAB_REQUEST;
			}
		}
	};
}