package ge.altasoft.gia.cha;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    static String mqttBrokerLocalUrl = "192.168.1.23:1883";
    static String mqttBrokerGlobalUrl = "212.72.150.154:1883";

    static MqttClientLocal.MQTTConnectionStatus mqttConnectionStatus;
    static String lastMqttConnectionStatusMessage = "";
    static String lastMqttConnectionErrorMessage = "";
    static long lastMqttConnectionErrorMessageTime = 0;
    static boolean lastMqttConnectionWrtIsOnline = false;

    public final static int COLOR_TEMP_HIGH = 0xFFFF3000;
    public final static int COLOR_TEMP_LOW = 0xFF00DDFF;
    public final static int COLOR_TEMP_NORMAL = Color.WHITE;
    private final static int COLOR_CARD_BK = 0xFF424242;
    private final static int COLOR_CARD_BK_ERROR = 0xFF500000;
    private final static int COLOR_CARD_BK_PRESSED = Color.GRAY;

    public final static float F_UNDEFINED = 999.9f;
    public final static float I_UNDEFINED = 32767;

    final static int ERR_GENERAL = 1;

    // Thermostat controller errors
    final static int ERR_SENSOR = 2;
    final static int ERR_EMOF = 4;
    final static int ERR_95_DEGREE = 8;
    final static int ERR_CFR = 16;
    final static int ERR_SMX = 32;
    final static int ERR_T1 = 64;
    final static int ERR_T2 = 128;
    final static int ERR_T3 = 256;
    final static int ERR_TF = 512; // Furnace or burner

    // Water level controller errors
    final static int ERR_ULTRASONIC_1 = 2;
    final static int ERR_ULTRASONIC_2 = 4;
    final static int ERR_ULTRASONIC_3 = 8;


    static final int ACTIVITY_REQUEST_RESULT_SETTINGS = 1;
    static final int ACTIVITY_REQUEST_RESULT_LIGHT_SETTINGS = 2;
    final static int ACTIVITY_REQUEST_RESULT_THERMOSTAT_SETTINGS = 3;
    final static int ACTIVITY_REQUEST_RESULT_WATER_LEVEL_SETTINGS = 4;


    public static String shortToHex4(short value) {
        return String.format(Locale.US, "%04X", value);
    }

    static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static float decodeT(String s) {
        int value = Integer.parseInt(s, 16);
        return (value - 1000) / 10f;
    }

    public static void encodeT(StringBuilder sb, float T) {
        if (Float.isNaN(T))
            T = Utils.F_UNDEFINED * 10f;
        else
            T = T * 10f;
        sb.append(String.format(Locale.US, "%04X", ((Float) (T + 1000)).intValue()));
    }

    public static void encodeTime(StringBuilder sb, int t) {
        sb.append(String.format(Locale.US, "%04X", t));
    }

    static void readUrlSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String url = prefs.getString("mtqq_url_local", "");
        if (url.equals("")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("mtqq_url_local", mqttBrokerLocalUrl);
            editor.apply();
        } else
            mqttBrokerLocalUrl = url;

        url = prefs.getString("mtqq_url_global", "");
        if (url.equals("")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("mtqq_url_global", mqttBrokerGlobalUrl);
            editor.apply();
        } else
            mqttBrokerGlobalUrl = url;
    }

//    public static String millisToTimeString(String format, double x) {
//        if (format == null)
//            format = "HH:mm";
//        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis((long) x);
//        return sdf.format(calendar.getTimeInMillis());
//    }


    @NonNull
    public static String decodeArduinoString(String encodedName) {
        StringBuilder sb = new StringBuilder();
        boolean isUnicode = false;
        char[] carr = encodedName.toCharArray();
        for (char c : carr) {
            if (c == '~') {
                isUnicode = !isUnicode;
                continue;
            }

            if (isUnicode)
                sb.append((char) (((int) c + 0x108F))); // 'ა' - 'A'
            else
                sb.append(c);
        }
        return sb.toString();
    }

    public static String encodeArduinoString(String name) {
        StringBuilder sb = new StringBuilder();

        char[] carr = name.toCharArray();
        boolean isUnicode = false;
        for (char c : carr) {
            if (c == ';')
                c = ':';
            else if (c == '~')
                c = ' ';
            else if (((int) c >= 0x10D0) && ((int) c <= 0x10F0)) // 'ა'..'ჰ'
            {
                if (!isUnicode) {
                    isUnicode = true;
                    sb.append('~');
                }
                sb.append((char) (((int) c - 0x108F))); // 'ა' - 'A'
            } else {
                if (isUnicode) {
                    isUnicode = false;
                    sb.append('~');
                }
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static String getMtqqBrokerUrl(Context context) {
        if (Debug.isDebuggerConnected())
            //return mqttBrokerLocalUrl;
            return mqttBrokerGlobalUrl;

        final ConnectivityManager conMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr == null)
            return null;
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if ((activeNetwork == null) || (!activeNetwork.isConnected()))
            return null;


        if (activeNetwork.getType() != ConnectivityManager.TYPE_WIFI)
            return mqttBrokerGlobalUrl;

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return mqttBrokerGlobalUrl;

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null)
            return mqttBrokerGlobalUrl;

        if (!Utils.isGiaWifi(wifiInfo))
            return mqttBrokerGlobalUrl;

        return mqttBrokerLocalUrl;
    }

    static String getNetworkInfo(Context context) {
        if (Debug.isDebuggerConnected())
            return "Inside Debugger";

        final ConnectivityManager conMgr = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr == null)
            return "No connectivity service";

        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork == null)
            return "No active network";
        if (!activeNetwork.isConnected())
            return "Not connected to network";

        if (activeNetwork.getType() != ConnectivityManager.TYPE_WIFI)
            return "No WiFi";

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return "No WiFi manager";

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null)
            return "No WiFi info";

        if (!isGiaWifi(wifiInfo))
            return "SSID = ".concat(wifiInfo.getSSID());

        return null;
    }

    private static boolean isGiaWifi(WifiInfo wifiInfo) {
        return wifiInfo.getSSID().trim().equals("\"GIA2\"");
    }

//    public static int random(int min, int max) {
//        return min + (int) Math.round(Math.random() * (max - min));
//    }

    static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String device = Build.SERIAL;

        BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        if (ba != null)
            device = ba.getName();

        if (model.startsWith(manufacturer))
            model = capitalize(model);
        else
            model = capitalize(manufacturer) + " " + model;

        return model.concat(" - ").concat(device);
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

//    static String getDeviceUniqueId(Context context) {
//        return Build.SERIAL.concat(".").concat(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
//    }

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

    public static int getCardBackgroundColor(boolean isPressed, boolean isError) {
        int color;
        if (isPressed)
            color = COLOR_CARD_BK_PRESSED;
        else if (isError)
            color = COLOR_CARD_BK_ERROR;
        else
            color = COLOR_CARD_BK;

        return color;
    }

    public static long DecodeTime(String time) {

        int yr, month, day;
        int hr, min, sec;

        int idx = 0;
        yr = 2000 + (time.charAt(idx++) - '0') * 10 + (time.charAt(idx++) - '0');
        month = (time.charAt(idx++) - '0') * 10 + (time.charAt(idx++) - '0');
        day = (time.charAt(idx++) - '0') * 10 + (time.charAt(idx++) - '0');
        idx++;
        hr = (time.charAt(idx++) - '0') * 10 + (time.charAt(idx++) - '0');
        min = (time.charAt(idx++) - '0') * 10 + (time.charAt(idx++) - '0');
        sec = (time.charAt(idx++) - '0') * 10 + (time.charAt(idx++) - '0');

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(yr, month - 1, day, hr, min, sec);

        return calendar.getTimeInMillis();
    }

    public static String GetBallValveStateText(int state) {
        if (state == 0xFF) {
            return "On";
        } else if (state == 0xFF01) {
            return "Off";
        } else if (state < 0xFF) {
            return ">>>";
        } else {
            return "<<<";
        }
    }

    public static int GetBallValveStateColor(int state) {
        if (state == 0xFF) {
            return Color.GREEN;
        } else if (state == 0xFF01) {
            return Color.RED;
        } else {
            return Color.YELLOW;
        }
    }

//    public static void analyseStorage(Context context) {
//        long totalSize = 0;
//        File appBaseFolder = context.getFilesDir().getParentFile();
//        for (File f : appBaseFolder.listFiles()) {
//            if (f.isDirectory()) {
//                long dirSize = browseFiles(f);
//                totalSize += dirSize;
//                Log.d("CHA", f.getPath() + " uses " + dirSize + " bytes");
//            } else {
//                totalSize += f.length();
//            }
//        }
//        Log.d("CHA", "App uses " + totalSize + " total bytes");
//    }

//    private static long browseFiles(File dir) {
//        long dirSize = 0;
//        for (File f : dir.listFiles()) {
//            dirSize += f.length();
//            //Log.d(STORAGE_TAG, dir.getAbsolutePath() + "/" + f.getName() + " weighs " + f.length());
//            if (f.isDirectory()) {
//                dirSize += browseFiles(f);
//            }
//        }
//        return dirSize;
//    }
//    public static int getColorFromResource(Context context, int resId) {
//        return ContextCompat.getColor(context, resId);
//    }

    //    public static void InputDialog(Context context, String title, String value, int type, final RunnableWithParams positiveAction) {
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(title);
//
//        final EditText input = new EditText(context);
//        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//        input.setInputType(type);
//        input.setText(value);
//        builder.setView(input);
//
//        // Set up the buttons
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String strValue = input.getText().toString();
//                if (positiveAction != null)
//                    positiveAction.run(strValue);
//                dialog.dismiss();
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        AlertDialog alert = builder.create();
//        alert.show();
//    }
}
