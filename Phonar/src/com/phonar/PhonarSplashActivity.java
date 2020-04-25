package com.phonar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

import com.phonar.utils.PhonarPreferencesManager;

/**
 * Splash page
 */
public class PhonarSplashActivity extends Activity {
	// thread to wait a few seconds
	private Thread waitThread;

	private final long WAIT_TIME = 1 * 1000;
	private String lock = "lock";
	private boolean mDone = false;
	private RotateAnimation animation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		mDone = false;

		// Do Rotation Effect
		View icon = findViewById(R.id.splash_icon);
		animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(WAIT_TIME);
		animation.setFillAfter(true);
		icon.startAnimation(animation);

		// TextView splash_text = (TextView)
		// findViewById(R.id.splash_text_view);
		RelativeLayout splash_main = (RelativeLayout) findViewById(R.id.splash_main_view);

		// wait for x seconds
		waitThread = new Thread() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						wait(WAIT_TIME);
					}
				} catch (InterruptedException ex) {
				}
				synchronized (lock) {
					if (mDone == false) {
						mDone = true;
						afterAnimation();
					}
				}
			}
		};
		splash_main.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				synchronized (lock) {
					if (mDone == false) {
						mDone = true;
						afterAnimation();
					}
				}
			}
		});

		waitThread.start();
	}

	private void afterAnimation() {
		if (PhonarPreferencesManager.isFriendsMode(PhonarSplashActivity.this)) {
			// ensure we have phone number and c2dm id
			if (PhonarPreferencesManager.getPhoneNumber(PhonarSplashActivity.this) == null
							|| PhonarPreferencesManager.getC2DMId(PhonarSplashActivity.this) == null
							|| PhonarPreferencesManager.isGoogleVoice(PhonarSplashActivity.this) == null) {
				Intent intent = new Intent(PhonarSplashActivity.this, PhoneNumberActivity.class);
				PhonarSplashActivity.this.startActivity(intent);
				PhonarSplashActivity.this.finish();
				return;
			}
		}
		// start tab activity
		PhonarSplashActivity.this.startActivity(new Intent(PhonarSplashActivity.this,
						PhonarTabActivity.class));
		PhonarSplashActivity.this.finish();
	}
}
