package com.phonar.request;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.phonar.R;
import com.phonar.network.NetworkRequest;

public class AddOptionsActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addoptions);

		// Add device button
		findViewById(R.id.adddevice).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AddOptionsActivity.this,
								com.google.zxing.client.android.CaptureActivity.class);

				// This flag clears the called app from the activity
				// stack, so
				// users arrive in the expected
				// place next time this application is restarted.
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				startActivityForResult(intent, RequestActivity.QR_INTENT_CODE);
			}
		});

		// Order device button
		findViewById(R.id.orderdevice).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = NetworkRequest.URL_ORDER_DEVICE;
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
				AddOptionsActivity.this.finish();
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		/*
		 * returning from ZXing
		 */
		if (requestCode == RequestActivity.QR_INTENT_CODE) {
			if (resultCode == RESULT_OK) {
				RequestActivity.onQRScanned(this, intent);
			}
		}
		finish();
	}

}
