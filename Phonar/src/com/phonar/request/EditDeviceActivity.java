package com.phonar.request;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.phonar.DevicesList;
import com.phonar.PhonarActivity;
import com.phonar.R;
import com.phonar.db.PhonarDatabase;
import com.phonar.models.Device;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.ImageUtils;

public class EditDeviceActivity extends PhonarActivity {

    private Bitmap image;
    private String phoneNumber;
    private String password;
    private String type;
    private boolean isNew;

    public static final String EXTRA_NEW = "new?";
    public static final String EXTRA_PHONE = "phone";
    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_IMAGE = "image";
    public static final String EXTRA_INTERVAL = "interval";
    public static final String EXTRA_POLLS = "polls";

    private static final int CAPTURE_INTENT = 0;
    private static final int SELECT_INTENT = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editdevice);

        // programmatically set form to be above button at bottom and below
        // header
        ScrollView form = (ScrollView) findViewById(R.id.edit_device_form);

        RelativeLayout.LayoutParams layoutparams =
            (RelativeLayout.LayoutParams) form.getLayoutParams();
        layoutparams.addRule(RelativeLayout.BELOW, R.id.edit_device_title_view);
        layoutparams.addRule(RelativeLayout.ABOVE, R.id.submit);
        form.setLayoutParams(layoutparams);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            // got here through third party barcode scanner
            String params[] = intent.getDataString().split("\\?");
            phoneNumber = params[1];
            password = params[2];
            type = params[3];
            image = BitmapFactory.decodeResource(getResources(), R.drawable.androidmarker);
        } else {
            // got here through our embedded barcode scanner, or selecting
            // "edit device"
            phoneNumber = getIntent().getStringExtra(EXTRA_PHONE);
            password = getIntent().getStringExtra(EXTRA_PASSWORD);
            type = getIntent().getStringExtra(EXTRA_TYPE);
            if ((isNew = getIntent().getBooleanExtra(EXTRA_NEW, true)) == false) {
                // editing existing device
                ((EditText) findViewById(R.id.name))
                    .setText(getIntent().getStringExtra(EXTRA_NAME));
                image = getIntent().getParcelableExtra(EXTRA_IMAGE);
                ((EditText) findViewById(R.id.interval)).setText(Long.toString(getIntent()
                    .getLongExtra(EXTRA_INTERVAL, 0L)));
                ((EditText) findViewById(R.id.polls)).setText(Long.toString(getIntent()
                    .getLongExtra(EXTRA_POLLS, 0L)));
            } else {
                // making new device
                image = BitmapFactory.decodeResource(getResources(), R.drawable.androidmarker);
                ((EditText) findViewById(R.id.interval)).setText(Long.toString(0L));
                ((EditText) findViewById(R.id.polls)).setText(Long.toString(0L));
            }
        }

        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(ImageUtils.getPaddedBitmap(image));

        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final CharSequence[] items =
                    {
                        EditDeviceActivity.this.getString(R.string.captureimage),
                        EditDeviceActivity.this.getString(R.string.selectimage) };
                AlertDialog.Builder builder = new AlertDialog.Builder(EditDeviceActivity.this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                            EditDeviceActivity.this.startActivityForResult(intent, CAPTURE_INTENT);
                        } else if (item == 1) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_INTENT);
                        }
                    }
                });
                CommonUtils.showDialog(builder);
            }
        });

        findViewById(R.id.submit).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String name = ((EditText) findViewById(R.id.name)).getText().toString();
                Long interval = Long.valueOf(DevicesList.TRACK_INTERVAL);
                Long polls = Long.valueOf(DevicesList.NUM_POLLS);

                if (isNew) {
                    Device device =
                        new Device(
                            EditDeviceActivity.this, phoneNumber, password, type, name,
                            EditDeviceActivity.this.image, null, null, null, interval, polls, null);
                    PhonarDatabase.add(EditDeviceActivity.this, device);
                    DevicesList.add(EditDeviceActivity.this, device);
                    device.sendNetworkRequestFindDevice(EditDeviceActivity.this
                        .getApplicationContext());
                } else {
                    PhonarDatabase.updateDeviceInfo(
                        EditDeviceActivity.this, phoneNumber, name, EditDeviceActivity.this.image);
                    Device device = DevicesList.get(EditDeviceActivity.this, phoneNumber);
                    device.displayName = name;
                    device.image = EditDeviceActivity.this.image;
                }
                finish();
            }

        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == CAPTURE_INTENT) {
            if (resultCode == RESULT_OK) {
                Bundle extras = intent.getExtras();
                image = ImageUtils.compressBitmap((Bitmap) extras.get("data"));
                ImageView imageView = (ImageView) findViewById(R.id.image);
                imageView.setImageBitmap(ImageUtils.getPaddedBitmap(image));
            }
        } else if (requestCode == SELECT_INTENT) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = intent.getData();
                try {
                    image =
                        ImageUtils.compressBitmap(ImageUtils.getImageFromURI(this, selectedImage));
                    ImageView imageView = (ImageView) findViewById(R.id.image);
                    imageView.setImageBitmap(ImageUtils.getPaddedBitmap(image));
                } catch (Exception e) {
                }
            }

        }
    }

    /**
     * Called when activity is first created and every time it resumes after
     * pause
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Whenever activity is paused or exited
     */
    @Override
    protected void onPause() {
        super.onPause();
    }
}
