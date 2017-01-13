package ge.altasoft.gia.cha.light;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import ge.altasoft.gia.cha.Utils;

public class LightBroadcastService extends Service {

    public static Boolean SetAllSettings = false;

    public static final String BROADCAST_ACTION_GET = "home.gia.light.get_data";
    public static final String BROADCAST_ACTION_SET = "home.gia.light.set_data";
    private final Handler handler = new Handler();

    private Intent intentBroadcastSender;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        LightUtils.LocalIP = prefs.getString("light_controller_ip", LightUtils.LocalIP);
        LightUtils.REFRESH_TIMEOUT = Integer.parseInt(prefs.getString("light_controller_polling", Integer.toString(LightUtils.REFRESH_TIMEOUT)));

        intentBroadcastSender = new Intent(BROADCAST_ACTION_GET);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        registerReceiver(broadcastReceiver, new IntentFilter(LightBroadcastService.BROADCAST_ACTION_SET));

        handler.postDelayed(sendUpdatesToUI, 0);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(sendUpdatesToUI);
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData(intent);
        }
    };


    private class AsyncAction extends AsyncTask<String, Void, String> {
        private Socket socket;

        protected String doInBackground(String... args) {
            String response = null;

            if (Utils.DEBUG_LIGHT) {
                if (args[0].startsWith("#")) {
                    LightControllerData.Instance.relays(Integer.parseInt(String.valueOf(args[0].charAt(1)), 16) - 1).setIsOn(args[0].charAt(2) == '1');
                    response = LightControllerData.Instance.encodeState();
                } else if (args[0].equals("M")) {
                    LightControllerData.Instance.setIsActive(false);
                    response = LightControllerData.Instance.encodeState();
                } else if (args[0].equals("A")) {
                    LightControllerData.Instance.setIsActive(true);
                    response = LightControllerData.Instance.encodeState();
                } else if (args[0].equals("?") || args[0].equals("@") || args[0].startsWith("*") || args[0].startsWith("#")) {
                    response = LightControllerData.Instance.encodeState();
                }

                if (args[0].equals("@")) {
                    response = LightControllerData.Instance.encodeSettings() + response;
                    //"@TT0008S0007F0008S0007T0008S0007F0008S0007T0008S0007T0008S0007T0008S0007T0008S0007T0008S0007T0008S0007T0008S0007T0008S0007" + response;
                }
                if (response == null) {
                    Log.wtf("GetSensorMappingId", "what the fuck");
                }

                return response;
            }

            try {
                socket = new Socket();
                socket.setSoTimeout(2000);
                //socket.setKeepAlive(true);
                socket.connect(new InetSocketAddress(LightUtils.GetControllerIp(getApplicationContext()), LightUtils.ServerPort), 5000);
            } catch (UnknownHostException e1) {
                ShowMessage(e1.getMessage());
                //TO DO
                return null;
            } catch (IOException e1) {
                ShowMessage(e1.getMessage());
                //TO DO
                return null;
            }
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.print(args[0]);
                out.flush();

                response = in.readLine();

                socket.close();
            } catch (IOException e) {
                ShowMessage(e.getMessage());
            }

            return response; //returns what you want to pass to the onPostExecute()
        }

        protected void onPostExecute(String result) {
            //result is the data returned from doInbackground
            intentBroadcastSender.putExtra("response", result);
            sendBroadcast(intentBroadcastSender);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setData(Intent intent) {
        handler.removeCallbacks(sendUpdatesToUI);
        String command = intent.getStringExtra("command");
        sendDataToController(command);
        handler.postDelayed(sendUpdatesToUI, LightUtils.REFRESH_TIMEOUT * 1000);
    }


    private void sendDataToController(String data) {
        Log.d("Light send command", data);
        new AsyncAction().execute(data);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            if (SetAllSettings) {
                SetAllSettings = false;
                sendDataToController(LightControllerData.Instance.encodeSettings());
            } else {
                sendDataToController(LightControllerData.Instance.haveSettings() ? "?" : "@");
            }
            handler.postDelayed(this, LightUtils.REFRESH_TIMEOUT * 1000);
        }
    };


    private void ShowMessage(final String message) {

        handler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }, 100);
    }
}