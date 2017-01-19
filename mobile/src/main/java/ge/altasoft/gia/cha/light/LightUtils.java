package ge.altasoft.gia.cha.light;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Debug;

import ge.altasoft.gia.cha.Utils;

public final class LightUtils {

    public final static int ACTIVITY_REQUEST_SETTINGS_CODE = 2; // $ + 12 switches + autoatic_mode + datetime  + sunrise/sunset 123456789012TYYMMDDHHmmssxxxxxxxx. 34 chars

//    public static void sendCommandToController(Context context, String command) {
//        Intent intent;
//        intent = new Intent(LightBroadcastService.BROADCAST_ACTION_SET);
//        intent.putExtra("command", command);
//        context.sendBroadcast(intent);
//    }
}