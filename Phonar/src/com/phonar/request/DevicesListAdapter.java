package com.phonar.request;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.phonar.DevicesList;
import com.phonar.R;
import com.phonar.models.Device;
import com.phonar.utils.CommonUtils;

public class DevicesListAdapter extends BaseAdapter {

    private Context mContext;

    public DevicesListAdapter(Context context, ListView listView) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return DevicesList.getAll(mContext).size();
    }

    @Override
    public Object getItem(int i) {
        return DevicesList.getAll(mContext).get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View row, ViewGroup root) {
        if (row == null) {
            LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.device, root, false);
        }
        final Device device = DevicesList.getAll(mContext).get(i);
        ImageView image = (ImageView) row.findViewById(R.id.image);
        TextView main_text = (TextView) row.findViewById(R.id.main_text);
        TextView sub_text = (TextView) row.findViewById(R.id.sub_text);
        ToggleButton track_button = (ToggleButton) row.findViewById(R.id.toggle_button);
        View error = row.findViewById(R.id.error);
        View progress = row.findViewById(R.id.progress);
        image.setImageBitmap(device.image);
        main_text.setText(device.displayName);
        if (device.isFriend() && device.requestSent && !device.error) {
            sub_text.setVisibility(View.VISIBLE);
            sub_text.setText(mContext.getString(R.string.waiting));
        } else if (device.timeOfLastLocation != null) {
            sub_text.setVisibility(View.VISIBLE);
            sub_text.setText(mContext.getString(R.string.lastlocationat)
                + CommonUtils.getTimeString(mContext, System.currentTimeMillis()
                    - device.timeOfLastLocation));
        } else {
            sub_text.setVisibility(View.GONE);
        }
        progress.setVisibility(View.GONE);
        if (device.isFriend()) {
            track_button.setVisibility(View.GONE);
        } else {
            track_button.setVisibility(View.VISIBLE);
            track_button.setFocusable(false);
            if (DevicesList.isTracked(device.phoneNumber)) {
                track_button.setChecked(true);
            } else {
                track_button.setChecked(false);
            }
            track_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton view, boolean checked) {
                    if (checked) {
                        DevicesList.startTrackingDevice(mContext, device.phoneNumber);
                    } else {
                        DevicesList.stopTrackingDevice(mContext, device.phoneNumber, true);
                    }
                }

            });
        }
        if (device.isFriend() && device.error) {
            error.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
        }
        return row;
    }
}
