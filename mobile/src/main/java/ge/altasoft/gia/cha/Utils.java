package ge.altasoft.gia.cha;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.widget.EditText;

import java.util.Locale;

import ge.altasoft.gia.cha.classes.RunnableWithParams;

public class Utils {

    public final static boolean DEBUG_LIGHT = false;
    public final static boolean DEBUG_THERMOSTAT = true;

    public static final int FLAG_HAVE_NOTHING = 0;
    public static final int FLAG_HAVE_STATE = 1;
    public static final int FLAG_HAVE_SETTINGS = 2;

    public static final int LOG_BUFFER_SIZE = 100;

    static final int ACTIVITY_REQUEST_SETTINGS_CODE = 1;

    public static final double DEFAULT_DESIRED_TEMPERATURE = 25.0;

    public static boolean disableOnCheckedListener = false;

    public static String ShortToHex4(short value) {
        return String.format(Locale.US, "%04X", value);
    }

    static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @NonNull
    public static String DecodeArduinoString(String encodedName) {
        StringBuilder sb = new StringBuilder();
        boolean prevCharIsEscape = false;
        char[] carr = encodedName.toCharArray();
        for (char c : carr) {
            if (c == '~') {
                prevCharIsEscape = true;
                continue;
            }

            if (prevCharIsEscape)
                sb.append((char) (((int) c + 0x108F))); // 'ა' - 'A'
            else
                sb.append(c);
            prevCharIsEscape = false;
        }
        return sb.toString();
    }

    public static String EncodeArduinoString(String name) {
        StringBuilder sb = new StringBuilder();

        char[] carr = name.toCharArray();
        for (char c : carr) {
            if (c == ';')
                sb.append(':');
            else if (c == '~')
                sb.append(' ');
            else if (((int) c >= 0x10D0) && ((int) c <= 0x10F0)) // 'ა'..'ჰ'
            {
                sb.append('~');
                sb.append((char) (((int) c - 0x108F))); // 'ა' - 'A'
            } else
                sb.append(c);
        }
        return sb.toString();
    }

    static String GetNetworkInfo(Context context) {
        if (Debug.isDebuggerConnected())
            return "Inside Debugger";

        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr == null)
            return "No connectivity service";

        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork == null)
            return "No active network";
        if (!activeNetwork.isConnected())
            return "Not connected to network";

        if (activeNetwork.getType() != ConnectivityManager.TYPE_WIFI)
            return "No WiFi";

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return "No WiFi manager";

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null)
            return "No WiFi info";

        if (!IsGiaWifi(wifiInfo))
            return "SSID = ".concat(wifiInfo.getSSID());

        return null;
    }

    public static boolean IsGiaWifi(WifiInfo wifiInfo) {
        return wifiInfo.getSSID().trim().equals("\"GIA\"") || wifiInfo.getSSID().trim().equals("\"GIA2\"");
    }

    public static int random(int min, int max)
    {
        return min + (int)Math.round(Math.random() * (max - min));
    }

    public static void ConfirmDialog(Context context, String title, String message, final Runnable positiveAction, final Runnable negativeAction) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (positiveAction != null)
                            positiveAction.run();
                        dialog.dismiss();
                    }
                }
        );

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (negativeAction != null)
                            negativeAction.run();
                        dialog.dismiss();
                    }
                }
        );

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void InputDialog(Context context, String title, String value, int type, final RunnableWithParams positiveAction) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        final EditText input = new EditText(context);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(type);
        input.setText(value);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strValue = input.getText().toString();
                if (positiveAction != null)
                    positiveAction.run(strValue);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
