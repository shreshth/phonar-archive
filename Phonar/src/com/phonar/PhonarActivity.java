package com.phonar;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class PhonarActivity extends Activity {

	@Override
	protected void onPause() {
		super.onPause();
		PhonarTabActivity.enableSleep(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		PhonarTabActivity.disableSleep(this);
	}

	// replicate in PhonarMapActivity
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (PhonarTabActivity.mIsARRunning && PhonarTabActivity.mAR != null) {
				try {
					final LinearLayout addMenu = (LinearLayout) findViewById(R.id.add_button_menu_ar);
					final RelativeLayout allDevices = (RelativeLayout) ((ViewGroup) addMenu)
									.getChildAt(0);
					final RelativeLayout addDevice = (RelativeLayout) ((ViewGroup) addMenu)
									.getChildAt(2);
					final RelativeLayout settings = (RelativeLayout) ((ViewGroup) addMenu)
									.getChildAt(4);
					allDevices.setSelected(false);
					addDevice.setSelected(false);
					settings.setSelected(false);

					if (addMenu.getVisibility() == View.GONE) {
						addMenu.setVisibility(View.VISIBLE);
						addMenu.setAnimation(AnimationUtils.loadAnimation(PhonarTabActivity.mAR,
										R.anim.menu_fadein));
					}
					return true;
				} catch (Exception e) {
					return super.onKeyUp(keyCode, event);
				}
			} else {
				try {
					Intent intent = new Intent(this, PreferencesActivity.class);
					this.startActivity(intent);
					return true;
				} catch (Exception e) {
					return super.onKeyUp(keyCode, event);
				}
			}
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	// Replicate in PhonarMapActivity
	@Override
	public void onBackPressed() {
		// hide menu
		if (PhonarTabActivity.mIsARRunning && PhonarTabActivity.mAR != null) {
			LinearLayout addMenu = (LinearLayout) PhonarTabActivity.mAR
							.findViewById(R.id.add_button_menu_ar);
			if (addMenu.getVisibility() == View.VISIBLE) {
				addMenu.setAnimation(AnimationUtils.loadAnimation(this, R.anim.menu_fadeout));
				addMenu.setVisibility(View.GONE);
			} else {
				this.finish();
			}
		} else {
			super.onBackPressed();
		}
	}
}
