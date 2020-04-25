package com.phonar.ar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phonar.DeviceLocationListener;
import com.phonar.DevicesList;
import com.phonar.PhonarActivity;
import com.phonar.PhonarTabActivity;
import com.phonar.PreferencesActivity;
import com.phonar.R;
import com.phonar.request.AddOptionsActivity;
import com.phonar.request.ContactPickerActivity;
import com.phonar.request.RequestActivity;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.GeoUtils;
import com.phonar.utils.GeoUtils.UserLocationListener;
import com.phonar.utils.PhonarPreferencesManager;

/**
 * Main AR activity (accessed from tab at top)
 */
public class ARActivity extends PhonarActivity implements UserLocationListener,
				DeviceLocationListener {

	private CameraLayer mCameraPreview = null;
	private DevicesLayer mDevicesLayer = null;

	// dialog to say that user's location is not yet available
	private View noLocationDialog;

	private SensorManager sensorManager;
	private float[] gravity = new float[3];
	private float[] geomag = new float[3];
	private float[] rotationMatrix = new float[16];
	private float[] remapMatrix = new float[16];

	// last good known orientations values
	private float[] orientations = new float[3];

	// maximum magnetic field strength without disturbance
	private static final float MAX_MAGNETIC_STRENGH = 85f;

	private Location myLastLocation = null;

	// Constant used to handle smoothing (used in the lowPass function)
	private static final float ALPHA = 0.05f;

	// for efficiency reasons, we only do some calculations when needed
	private boolean orientationsStale = false;

	// whether we have already displayed the magnetic interference dialog
	boolean magneticInterferenceSeen;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ar);
		magneticInterferenceSeen = PhonarPreferencesManager.getMagneticInterferenceSeen(this);

		// set click listener for menu
		// Replicate in PhonarMapActivity
		final LinearLayout addMenu = (LinearLayout) findViewById(R.id.add_button_menu_ar);
		// all devices/recent contacts
		final RelativeLayout allDevices = (RelativeLayout) ((ViewGroup) addMenu).getChildAt(0);
		if (PhonarPreferencesManager.isFriendsMode(this)) {
			((ImageView) allDevices.getChildAt(0)).setImageResource(R.drawable.findfriend);
			((TextView) allDevices.getChildAt(1)).setText(R.string.all_contacts);
		} else {
			((ImageView) allDevices.getChildAt(0)).setImageResource(R.drawable.adddevice);
			((TextView) allDevices.getChildAt(1)).setText(R.string.all_devices);
		}
		allDevices.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ARActivity.this, RequestActivity.class);
				startActivity(intent);
			}
		});
		// add device/contact
		final RelativeLayout addDevice = (RelativeLayout) ((ViewGroup) addMenu).getChildAt(2);
		if (PhonarPreferencesManager.isFriendsMode(this)) {
			((TextView) addDevice.getChildAt(1)).setText(R.string.add_contact);
			addDevice.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ARActivity.this, ContactPickerActivity.class);
					startActivity(intent);
				}
			});
		} else {
			((TextView) addDevice.getChildAt(1)).setText(R.string.add_device);
			addDevice.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(ARActivity.this, AddOptionsActivity.class);
					startActivity(intent);
				}
			});
		}
		// settings
		final RelativeLayout settings = (RelativeLayout) ((ViewGroup) addMenu).getChildAt(4);
		settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ARActivity.this, PreferencesActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * Called when activity is first created and every time it resumes after
	 * pause
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onResume() {
		super.onResume();
		PhonarTabActivity.mIsRunning = true;
		PhonarTabActivity.mIsARRunning = true;
		PhonarTabActivity.mAR = this;

		// window stuff - landscape view, fullscreen, hide window title etc.
		Window win = getWindow();
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create devices layer and camera layer
		mDevicesLayer = new DevicesLayer(this);

		if (mDevicesLayer.getCamera() == null) {
			CommonUtils.toast(this, R.string.no_camera);
			PhonarTabActivity.mPhonarTabActivity.getTabHost().setCurrentTab(
							PhonarTabActivity.TAB_REQUEST);
			PhonarTabActivity.mCurrentChild = PhonarTabActivity.TAB_REQUEST;
			return;
		}

		mCameraPreview = new CameraLayer(this, mDevicesLayer);

		// Check out location
		if (GeoUtils.getLastKnownLocation(this) != null) {
			mDevicesLayer.gotLocation();
			if (noLocationDialog != null) {
				((ViewGroup) getWindow().getDecorView()).removeView(noLocationDialog);
				noLocationDialog = null;
			}
		} else {
			if (noLocationDialog == null) {
				noLocationDialog = getLayoutInflater().inflate(R.layout.dialog, null);

				RelativeLayout.LayoutParams x = new RelativeLayout.LayoutParams(
								ViewGroup.LayoutParams.FILL_PARENT,
								ViewGroup.LayoutParams.FILL_PARENT);

				((ViewGroup) getWindow().getDecorView()).addView(noLocationDialog, x);
			}
			GeoUtils.singleLocationUpdate(this);
		}

		// register device location listener
		DevicesList.registerListener(this);

		// register for user's own location
		GeoUtils.registerListener(this, this);

		// add camera view
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		((RelativeLayout) findViewById(R.id.ar)).addView(mCameraPreview, params);
		mCameraPreview.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// hide menu
				LinearLayout addMenu = (LinearLayout) ARActivity.this
								.findViewById(R.id.add_button_menu_ar);
				if (addMenu.getVisibility() == View.VISIBLE) {
					addMenu.setAnimation(AnimationUtils.loadAnimation(ARActivity.this,
									R.anim.menu_fadeout));
					addMenu.setVisibility(View.GONE);
				}
				return true;
			}

		});

		// register listeners for sensors
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensorManager.registerListener(goodListener,
						sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
						SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(goodListener,
						sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_GAME);
	}

	/**
	 * Whenever activity is paused or exited
	 */
	@Override
	protected void onPause() {
		super.onPause();
		PhonarTabActivity.mIsRunning = false;
		PhonarTabActivity.mIsARRunning = false;

		// Remove all views
		((RelativeLayout) findViewById(R.id.ar)).removeAllViews();

		// Release camera
		if (mDevicesLayer.getCamera() == null) {
			return;
		}
		mDevicesLayer.releaseCamera();

		// remove device location listener
		DevicesList.removeListener(this);

		// remove user's location listener
		GeoUtils.removeListener(this, this);

		// unregister sensor listener
		sensorManager.unregisterListener(goodListener);

		// hide menu
		LinearLayout addMenu = (LinearLayout) ARActivity.this.findViewById(R.id.add_button_menu_ar);
		if (addMenu.getVisibility() == View.VISIBLE) {
			addMenu.setAnimation(AnimationUtils.loadAnimation(this, R.anim.menu_fadeout));
			addMenu.setVisibility(View.GONE);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * User's own location is changed
	 */
	@Override
	public void onUserLocation(Location location) {
		myLastLocation = location;
		DevicesList.onUserLocation(this);
		if (mDevicesLayer != null) {
			mDevicesLayer.gotLocation();
			mDevicesLayer.adjustDistances();
		}
		if (noLocationDialog != null) {
			((ViewGroup) getWindow().getDecorView()).removeView(noLocationDialog);
			noLocationDialog = null;
		}
	}

	/*
	 * Devices location is changed
	 */
	@Override
	public void onDeviceLocation() {
		if (mDevicesLayer != null) {
			mDevicesLayer.adjustDistances();
		}
	}

	public Location getLastLocation() {
		return myLastLocation;
	}

	public float[] getOrientations() {
		if (orientationsStale == true) {
			orientationsStale = false;
			SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomag);
			SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X,
							SensorManager.AXIS_Z, remapMatrix);
			SensorManager.getOrientation(remapMatrix, orientations);

			// make sure values are in the right range
			for (int i = 0; i < 3; i++) {
				while (orientations[i] < -Math.PI) {
					orientations[i] += 2 * Math.PI;
				}
				while (orientations[i] > Math.PI) {
					orientations[i] -= 2 * Math.PI;
				}
			}
		}
		return orientations;
	}

	/**
	 * Listener for sensor
	 */
	private final SensorEventListener goodListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent evt) {
			int type = evt.sensor.getType();

			// Smoothing the sensor data a bit
			if (type == Sensor.TYPE_MAGNETIC_FIELD) {
				if (!magneticInterferenceSeen
								&& Math.abs(evt.values[0]) + Math.abs(evt.values[1])
												+ Math.abs(evt.values[2]) > MAX_MAGNETIC_STRENGH) {
					showDisturbanceNotification();
					magneticInterferenceSeen = true;
					PhonarPreferencesManager.setMagneticInteferenceSeen(ARActivity.this, true);
				}
				lowPass(evt.values, geomag);
			} else if (type == Sensor.TYPE_ACCELEROMETER) {
				lowPass(evt.values, gravity);
			}

			orientationsStale = true;
		}
	};

	/*
	 * Implements a simple low-pass filter. Used to smooth out sensor readings.
	 */
	protected float[] lowPass(float[] input, float[] output) {
		if (output == null) {
			return input;
		}

		for (int i = 0; i < input.length; i++) {
			output[i] = output[i] + ALPHA * (input[i] - output[i]);
		}
		return output;
	}

	public CameraLayer getCLayer() {
		return mCameraPreview;
	}

	private void showDisturbanceNotification() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.magnetic_interference)).setCancelable(true);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog alert_temp = null;
		try {
			alert_temp = builder.create();
			alert_temp.show();
		} catch (Exception e) {
		}
		final AlertDialog alert = alert_temp;
		alert.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (alert != null && alert.isShowing()) {
					alert.dismiss();
					return true;
				} else {
					return true;
				}
			}
		});

		Handler handler = null;
		handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					if (alert != null && alert.isShowing()) {
						alert.dismiss();
					}
				} catch (Exception e) {
				}
			}
		}, 3000);
	}
}
