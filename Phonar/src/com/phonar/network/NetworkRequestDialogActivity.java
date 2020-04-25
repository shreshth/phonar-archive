package com.phonar.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;

import com.phonar.R;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.NetworkUtils;

/**
 * Class to show a dialog to ask user to turn on wifi/3G.
 * 
 * Replicates NetworkRequest:setupNetwork()
 */
public class NetworkRequestDialogActivity extends Activity {
    private final Context mContext = this;
    private boolean startedSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!NetworkUtils.isEnabledData(mContext) && !NetworkUtils.isEnabledWifi(mContext)) {
            if (NetworkRequest.NETWORK_TURN_ON_DIALOG_SHOWN) {
                CommonUtils.toast(mContext, R.string.turn_on_network_toast);
                this.setResult(RESULT_OK);
                this.finish();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder
                .setTitle(R.string.network_not_connected).setMessage(R.string.turn_on_network)
                .setPositiveButton(R.string.yes, clicklistener).setNegativeButton(
                    R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            ((Activity) mContext).setResult(RESULT_OK);
                            ((Activity) mContext).finish();
                        }
                    }).setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        ((Activity) mContext).setResult(RESULT_OK);
                        ((Activity) mContext).finish();
                    }
                });

            NetworkRequest.NETWORK_TURN_ON_DIALOG_SHOWN = true;
            CommonUtils.showDialog(builder);
        } else if (!NetworkUtils.isConnectedData(mContext)
            && !NetworkUtils.isConnectedWifi(mContext)) {
            if (NetworkRequest.NETWORK_TURN_ON_DIALOG_SHOWN) {
                CommonUtils.toast(mContext, R.string.connect_network_toast);
                this.setResult(RESULT_OK);
                this.finish();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder
                .setTitle(R.string.network_not_connected).setMessage(R.string.connect_network)
                .setCancelable(false).setPositiveButton(R.string.yes, clicklistener)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        ((Activity) mContext).setResult(RESULT_OK);
                        ((Activity) mContext).finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        ((Activity) mContext).setResult(RESULT_OK);
                        ((Activity) mContext).finish();
                    }
                });
            NetworkRequest.NETWORK_TURN_ON_DIALOG_SHOWN = true;
            CommonUtils.showDialog(builder);
        } else {
            this.setResult(RESULT_OK);
            this.finish();
        }

    }

    private DialogInterface.OnClickListener clicklistener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            // This flag clears the called app from the activity stack, so
            // users arrive in the expected
            // place next time this application is restarted.
            startedSettings = true;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            ((Activity) mContext).startActivityForResult(intent, 0);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (this.startedSettings) {
            this.setResult(RESULT_OK);
            this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.setResult(RESULT_OK);
        this.finish();
    }
}
