package ge.altasoft.gia.cha.thermostat;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Debug;

import ge.altasoft.gia.cha.Utils;

public final class ThermostatUtils {

    public static String LocalIP = "192.168.2.201";
    public final static int ServerPort = 80;
    public static int REFRESH_TIMEOUT = 5; //sec

    public final static int ACTIVITY_REQUEST_SETTINGS_CODE = 3; // $ + 12 switches + autoatic_mode + datetime  + sunrise/sunset 123456789012TYYMMDDHHmmssxxxxxxxx. 34 chars

    public static void sendCommandToController(Context context, String command) {
        Intent intent;
        intent = new Intent(ThermostatBroadcastService.BROADCAST_ACTION_SET);
        intent.putExtra("command", command);
        context.sendBroadcast(intent);
    }

    public static String GetControllerIp(Context context) {
        if (Debug.isDebuggerConnected())
            return LocalIP;

        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr == null)
            return null;
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if ((activeNetwork == null) || (!activeNetwork.isConnected()))
            return null;

        String externalUrl = "178.134.95.18";

        if (activeNetwork.getType() != ConnectivityManager.TYPE_WIFI)
            return externalUrl;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null)
            return externalUrl;

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null)
            return externalUrl;

        if (!Utils.isGiaWifi(wifiInfo))
            return externalUrl;

        return LocalIP;
    }
}