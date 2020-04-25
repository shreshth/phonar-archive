package com.phonar.map;

import java.util.List;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.phonar.DeviceLocationListener;
import com.phonar.DevicesList;
import com.phonar.PhonarTabActivity;
import com.phonar.PreferencesActivity;
import com.phonar.R;
import com.phonar.models.Device;
import com.phonar.request.AddOptionsActivity;
import com.phonar.request.ContactPickerActivity;
import com.phonar.request.RequestActivity;
import com.phonar.utils.GeoUtils;
import com.phonar.utils.GeoUtils.UserLocationListener;
import com.phonar.utils.ImageUtils;
import com.phonar.utils.PhonarPreferencesManager;

/**
 * Main map activity (the one accessed from top tab)
 */
public class PhonarMapActivity extends MapActivity implements DeviceLocationListener,
				UserLocationListener {
	private MapView mapView = null;
	private int lat_max = Integer.MIN_VALUE;
	private int lat_min = Integer.MAX_VALUE;
	private int lng_max = Integer.MIN_VALUE;
	private int lng_min = Integer.MAX_VALUE;
	private PhonarItemizedOverlay itemizedoverlay;
	private MyLocationOverlay myLocationOverlay;
	private boolean init = false;
	private boolean initial_zoom = false;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		init = false;

		initMap();
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);

		// set click listener for menu
		// Replicate in ARActivity
		final LinearLayout addMenu = (LinearLayout) findViewById(R.id.add_button_menu_map);
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
				Intent intent = new Intent(PhonarMapActivity.this, RequestActivity.class);
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
					Intent intent = new Intent(PhonarMapActivity.this, ContactPickerActivity.class);
					startActivity(intent);
				}
			});
		} else {
			((TextView) addDevice.getChildAt(1)).setText(R.string.add_device);
			addDevice.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(PhonarMapActivity.this, AddOptionsActivity.class);
					startActivity(intent);
				}
			});
		}
		// settings
		final RelativeLayout settings = (RelativeLayout) ((ViewGroup) addMenu).getChildAt(4);
		settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PhonarMapActivity.this, PreferencesActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		PhonarTabActivity.disableSleep(this);

		// register listeners for other people's location
		DevicesList.registerListener(this);

		// register for user's own location
		GeoUtils.registerListener(this, this);

		Location cur_location = GeoUtils.getCurrentLocation(this);
		if (cur_location != null) {
			adjustWindowMyLocation(cur_location);
		}
		myLocationOverlay.enableMyLocation();
		showPeople();

		PhonarTabActivity.mIsRunning = true;
		PhonarTabActivity.mIsMapRunning = true;
		PhonarTabActivity.mMap = this;

		adjustZoom();
	}

	/**
	 * adjust zoom give devices and user's own location
	 */
	private void adjustZoom() {
		MapController controller = mapView.getController();
		// only zoom out
		if (mapView.getLatitudeSpan() < lat_max - lat_min
						|| mapView.getLongitudeSpan() < lng_max - lng_min || !init || !initial_zoom) {
			controller.zoomToSpan(Math.abs(lat_max - lat_min) * 2, Math.abs(lng_max - lng_min) * 2);
			initial_zoom = true;
		}
		controller.animateTo(new GeoPoint((lat_max + lat_min) / 2, (lng_max + lng_min) / 2));
	}

	@Override
	protected void onPause() {
		super.onPause();
		PhonarTabActivity.enableSleep(this);

		// remove other people's listener
		DevicesList.removeListener(this);

		// register for user's own location
		GeoUtils.removeListener(this, this);

		myLocationOverlay.disableMyLocation();

		PhonarTabActivity.mIsRunning = false;
		PhonarTabActivity.mIsMapRunning = false;

		// hide menu
		LinearLayout addMenu = (LinearLayout) PhonarMapActivity.this
						.findViewById(R.id.add_button_menu_map);
		if (addMenu.getVisibility() == View.VISIBLE) {
			addMenu.setAnimation(AnimationUtils.loadAnimation(this, R.anim.menu_fadeout));
			addMenu.setVisibility(View.GONE);
		}
	}

	/**
	 * Initialize the map view
	 */
	private void initMap() {
		// get mapview and make zoomable
		mapView = (PhonarMapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.displayZoomControls(true);

		// Center button
		final ImageButton centerButton = (ImageButton) findViewById(R.id.mapcenterbutton);
		centerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				adjustZoom();

				// hide menu
				LinearLayout addMenu = (LinearLayout) PhonarMapActivity.this
								.findViewById(R.id.add_button_menu_map);
				if (addMenu.getVisibility() == View.VISIBLE) {
					addMenu.setAnimation(AnimationUtils.loadAnimation(PhonarMapActivity.this,
									R.anim.menu_fadeout));
					addMenu.setVisibility(View.GONE);
				}
			}
		});
	}

	/**
	 * Adjust lat, lng window using my location
	 */
	private void adjustWindowMyLocation(Location location) {
		if (location != null) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);

			if (lat < lat_min) {
				lat_min = lat;
			}
			if (lat > lat_max) {
				lat_max = lat;
			}
			if (lng < lng_min) {
				lng_min = lng;
			}
			if (lng > lng_max) {
				lng_max = lng;
			}

			// fix zoom first time
			if (!init) {
				adjustZoom();
				init = true;
			}
		}
	}

	/**
	 * Initialize people as map overlays
	 */
	private void showPeople() {
		boolean added = false; // to keep track if even one device is added

		// get overlays
		List<Overlay> mapOverlays = mapView.getOverlays();

		if (itemizedoverlay != null) {
			mapOverlays.remove(itemizedoverlay);
			itemizedoverlay.clear();
		}

		Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
		itemizedoverlay = new PhonarItemizedOverlay(drawable, this);

		// add people to itemized overlay
		List<Device> deviceList = DevicesList.getAll(this);
		if (deviceList != null) {
			for (Device device : deviceList) {
				if (device != null
								&& device.latitude != null
								&& device.longitude != null
								&& device.timeOfLastLocation != null
								&& System.currentTimeMillis() - device.timeOfLastLocation < PhonarTabActivity.MAX_AGE) {
					addOverlayItem(itemizedoverlay, device.latitude, device.longitude,
									device.phoneNumber, device.image, device.timeOfLastLocation);
					added = true;
				}
			}
		}

		// set itemized overlay as map overlay
		if (added) {
			mapOverlays.add(itemizedoverlay);
		}
	}

	/**
	 * Add overlay item. SEE PARAMS BELOW
	 * 
	 * @param phoneNumber
	 *            For devices, this is phone number. For friends, this is lookup
	 *            key
	 */
	private void addOverlayItem(PhonarItemizedOverlay itemizedoverlay, Double latitude,
					Double longitude, String phoneNumber, Bitmap image, Long timeOfLastLocation) {
		int lat = (int) (latitude * 1E6);
		int lng = (int) (longitude * 1E6);

		if (lat < lat_min) {
			lat_min = lat;
		}
		if (lat > lat_max) {
			lat_max = lat;
		}
		if (lng < lng_min) {
			lng_min = lng;
		}
		if (lng > lng_max) {
			lng_max = lng;
		}

		GeoPoint point = new GeoPoint(lat, lng);
		OverlayItem overlayitem = new OverlayItem(point, "", phoneNumber);

		// set image
		Bitmap bMap = image;
		Drawable deviceDrawable;
		// grey out image if more than one hour
		long curTime = System.currentTimeMillis();
		long lastTime = timeOfLastLocation;
		if (curTime - lastTime > PhonarTabActivity.STALE_AGE) {
			Bitmap newBMap = ImageUtils.toGrayscale(bMap);
			deviceDrawable = new BitmapDrawable(getResources(), ImageUtils.getPaddedBitmap(newBMap));
		} else {
			deviceDrawable = new BitmapDrawable(getResources(), ImageUtils.getPaddedBitmap(bMap));
		}
		deviceDrawable.setBounds((int) (-0.5 * deviceDrawable.getIntrinsicWidth()),
						(int) (-0.5 * deviceDrawable.getIntrinsicHeight()),
						(int) (0.5 * deviceDrawable.getIntrinsicWidth()),
						(int) (0.5 * deviceDrawable.getIntrinsicHeight()));
		overlayitem.setMarker(deviceDrawable);

		itemizedoverlay.addOverlay(overlayitem);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onDeviceLocation() {
		showPeople();
	}

	@Override
	public void onUserLocation(Location location) {
		DevicesList.onUserLocation(this);
		adjustWindowMyLocation(location);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			try {
				final LinearLayout addMenu = (LinearLayout) findViewById(R.id.add_button_menu_map);
				final RelativeLayout addDevice = (RelativeLayout) ((ViewGroup) addMenu)
								.getChildAt(0);
				final RelativeLayout buyDevice = (RelativeLayout) ((ViewGroup) addMenu)
								.getChildAt(2);
				final RelativeLayout settings = (RelativeLayout) ((ViewGroup) addMenu)
								.getChildAt(4);
				addDevice.setSelected(false);
				buyDevice.setSelected(false);
				settings.setSelected(false);

				if (addMenu.getVisibility() == View.GONE) {
					addMenu.setVisibility(View.VISIBLE);
					addMenu.setAnimation(AnimationUtils.loadAnimation(PhonarMapActivity.this,
									R.anim.menu_fadein));
				}
				return true;
			} catch (Exception e) {
				return super.onKeyUp(keyCode, event);
			}
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public void onBackPressed() {
		LinearLayout addMenu = (LinearLayout) PhonarMapActivity.this
						.findViewById(R.id.add_button_menu_map);
		if (addMenu.getVisibility() == View.VISIBLE) {
			addMenu.setAnimation(AnimationUtils.loadAnimation(this, R.anim.menu_fadeout));
			addMenu.setVisibility(View.GONE);
		} else {
			this.finish();
		}
	}

}
