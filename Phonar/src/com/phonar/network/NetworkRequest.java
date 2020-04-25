package com.phonar.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

import com.phonar.PhonarTabActivity;
import com.phonar.R;
import com.phonar.utils.CommonUtils;
import com.phonar.utils.NetworkUtils;
import com.phonar.utils.log;

/**
 * 
 * Use this generic helper to make network requests
 * 
 */
public class NetworkRequest extends AsyncTask<String, Void, String> {

    public static final String URL_ADDDEVICE_DEV = "http://dev.phonar.me/add.php";
    public static final String URL_ADDDEVICE_PROD = "http://phonar.me/add.php";

    public static final String URL_FINDONCE = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/findonce" : "http://dev.phonar.me/findonce";
    public static final String URL_TRACK = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/track" : "http://dev.phonar.me/track";
    public static final String URL_STOPTRACKING = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/stoptracking" : "http://dev.phonar.me/stoptracking";
    public static final String URL_GETLOCATION = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/getlocation" : "http://dev.phonar.me/getlocation";
    public static final String URL_ADDDEVICE = CommonUtils.PRODUCTION_MODE
        ? URL_ADDDEVICE_PROD : URL_ADDDEVICE_DEV;
    public static final String URL_SOS = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/sos" : "http://dev.phonar.me/sos";
    public static final String URL_FIND_FRIEND = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/findfriend" : "http://dev.phonar.me/findfriend";
    public static final String URL_VERIFY_PHONENUMBER = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/verifyphonenumber" : "http://dev.phonar.me/verifyphonenumber";
    public static final String URL_REGISTER_PHONE = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/registerphone" : "http://dev.phonar.me/registerphone";
    public static final String URL_ON_FRIEND_LOCATION = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/onfriendlocation" : "http://dev.phonar.me/onfriendlocation";
    public static final String URL_LOCATE = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/locate" : "http://dev.phonar.me/locate";
    public static final String URL_SHORT_URL = CommonUtils.PRODUCTION_MODE
        ? "http://phnr.net/l" : "http://dev.phnr.net/l";
    public static final String URL_ORDER_DEVICE = CommonUtils.PRODUCTION_MODE
        ? "http://phonar.me/preorder" : "http://dev.phonar.me/preorder";

    public static boolean NETWORK_TURN_ON_DIALOG_SHOWN = false;

    // Maximum number of attempts before giving up
    private static final int MAX_ATTEMPTS = 3;

    private String url;
    private boolean isPost; // true if POST, false if GET
    private List<NameValuePair> parameters; // GET or POST parameters
    private int attempts = 0;
    private String data = null;
    private NetworkRequestListener listener;
    private Context context;

    public NetworkRequest(
        Context context, String url, boolean isPost, List<NameValuePair> parameters,
        NetworkRequestListener listener) {
        this.url = url;
        this.isPost = isPost;
        if (parameters == null) {
            this.parameters = new ArrayList<NameValuePair>();
        } else {
            this.parameters = parameters;
        }
        // add version
        Integer version = CommonUtils.getVersion(context, 9999);

        parameters.add(new BasicNameValuePair("version", version.toString()));
        // add app type
        parameters.add(new BasicNameValuePair("APP_TYPE", "android"));
        this.listener = listener;
        this.context = context;
    }

    public void run() {
        setupNetwork(context, false);

        if (!NetworkUtils.isEnabledData(context) && !NetworkUtils.isEnabledWifi(context)) {
            if (listener != null) {
                listener.onRequestComplete(null);
            }
            return;
        }

        this.execute(url);
    }

    public static void setupNetwork(Context context, boolean forceDialog) {
        final Context context_use =
            PhonarTabActivity.mPhonarTabActivity == null
                ? context : PhonarTabActivity.mPhonarTabActivity;

        if (!NetworkUtils.isEnabledData(context) && !NetworkUtils.isEnabledWifi(context)) {
            if (NETWORK_TURN_ON_DIALOG_SHOWN && !forceDialog) {
                CommonUtils.toast(context_use, R.string.turn_on_network_toast);
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context_use);
            builder
                .setTitle(R.string.network_not_connected).setMessage(R.string.turn_on_network)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        // This flag clears the called app from the activity
                        // stack, so
                        // users arrive in the expected
                        // place next time this application is restarted.
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        context_use.startActivity(intent);
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
            NETWORK_TURN_ON_DIALOG_SHOWN = true;
            CommonUtils.showDialog(builder);
        } else if (!NetworkUtils.isConnectedData(context) && !NetworkUtils.isConnectedWifi(context)) {
            if (NETWORK_TURN_ON_DIALOG_SHOWN && !forceDialog) {
                CommonUtils.toast(context_use, R.string.connect_network_toast);
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context_use);
            builder
                .setTitle(R.string.network_not_connected).setMessage(R.string.connect_network)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                        // This flag clears the called app from the activity
                        // stack, so
                        // users arrive in the expected
                        // place next time this application is restarted.
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                        context_use.startActivity(intent);
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
            NETWORK_TURN_ON_DIALOG_SHOWN = true;
            CommonUtils.showDialog(builder);
        }
    }

    @Override
    protected String doInBackground(String... urls) {
        url = urls[0];
        HttpClient httpclient = new DefaultHttpClient();
        if (isPost) {
            boolean success = false;
            while (attempts < MAX_ATTEMPTS && success == false) {
                attempts++;
                HttpPost httppost = new HttpPost(url);
                try {
                    httppost.setEntity(new UrlEncodedFormEntity(parameters));
                    HttpResponse response = httpclient.execute(httppost);

                    if (response.getStatusLine().getStatusCode() == 200) {
                        success = true;
                        InputStream instream = response.getEntity().getContent();
                        BufferedReader br =
                            new BufferedReader(new InputStreamReader(instream, "UTF-8"));
                        StringBuilder builder = new StringBuilder();
                        String aux = "";
                        while ((aux = br.readLine()) != null) {
                            builder.append(aux);
                        }
                        data = builder.toString();
                    } else {
                        log.e("network_error", response.getStatusLine().toString());
                    }
                    response.getEntity().consumeContent();
                } catch (ClientProtocolException e) {
                    log.e("network_error", e.toString());
                } catch (IOException e) {
                    log.e("network_error", e.toString());
                }
            }
            return data;
        } else {
            boolean success = false;
            while (attempts < MAX_ATTEMPTS && success == false) {
                attempts++;
                if (!parameters.isEmpty()) {
                    url += "?";
                }
                for (NameValuePair pair : parameters) {
                    try {
                        url +=
                            URLEncoder.encode(pair.getName(), "UTF-8")
                                + "=" + URLEncoder.encode(pair.getValue(), "UTF-8") + "&";
                    } catch (UnsupportedEncodingException e) {
                    }
                }
                if (!parameters.isEmpty()) {
                    url = url.substring(0, url.length() - 1);
                }
                HttpGet httpget = new HttpGet(url);
                try {
                    HttpResponse response = httpclient.execute(httpget);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        success = true;
                        InputStream instream = response.getEntity().getContent();
                        BufferedReader br =
                            new BufferedReader(new InputStreamReader(instream, "UTF-8"));
                        StringBuilder builder = new StringBuilder();
                        String aux = "";
                        while ((aux = br.readLine()) != null) {
                            builder.append(aux);
                        }
                        data = builder.toString();
                    }
                    response.getEntity().consumeContent();
                } catch (ClientProtocolException e) {
                } catch (IOException e) {
                }
            }
            return data;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (listener != null) {
            listener.onRequestComplete(result);
        }
    }

    public interface NetworkRequestListener {

        public void onRequestComplete(String response);

    }

}
