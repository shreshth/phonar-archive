package com.phonar.device;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class PhonarClientActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the SMS service
        Intent newinIntent = new Intent(this, ServiceCommunicator.class);
        this.startService(newinIntent);

        // // End the activity
        // finish();
    }
}
