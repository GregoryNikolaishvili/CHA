package ge.altasoft.gia.cha.thermostat;

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

public class ThermostatBroadcastService extends Service {

    public static Boolean SetAllSettings = false;

    public static final String BROADCAST_ACTION_GET = "home.gia.thermostat.get_data";
    public static final String BROADCAST_ACTION_SET = "home.gia.thermostat.set_data";
    private final Handler handler = new Handler();

    private Intent intentGet;

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ThermostatUtils.LocalIP = prefs.getString("thermostat_controller_ip", ThermostatUtils.LocalIP);
        ThermostatUtils.REFRESH_TIMEOUT = Integer.parseInt(prefs.getString("thermostat_controller_polling", Integer.toString(ThermostatUtils.REFRESH_TIMEOUT)));

        intentGet = new Intent(BROADCAST_ACTION_GET);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        registerReceiver(broadcastReceiver, new IntentFilter(ThermostatBroadcastService.BROADCAST_ACTION_SET));

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

            if (Utils.DEBUG_THEROSTAT) {
                if (args[0].startsWith("#")) {
                    ThermostatControllerData.Instance.relays(Integer.parseInt(String.valueOf(args[0].charAt(1)), 16) - 1)._setIsOn(args[0].charAt(2) == '1');
                    response = ThermostatControllerData.Instance.encodeState();
                } else if (args[0].equals("M")) {
                    ThermostatControllerData.Instance.setIsActive(false);
                    response = ThermostatControllerData.Instance.encodeState();
                } else if (args[0].equals("A")) {
                    ThermostatControllerData.Instance.setIsActive(true);
                    response = ThermostatControllerData.Instance.encodeState();
                } else if (args[0].equals("?") || args[0].equals("@") || args[0].startsWith("*") || args[0].startsWith("#")) {
                    response = ThermostatControllerData.Instance.encodeState();
                }

                if (args[0].equals("@")) {
                    response = ThermostatControllerData.Instance.encodeSettings() + response;
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
                socket.connect(new InetSocketAddress(ThermostatUtils.GetControllerIp(getApplicationContext()), ThermostatUtils.ServerPort), 5000);
            } catch (UnknownHostException e1) {
                ShowMessage(e1.getMessage());
                return null; //TODO
            } catch (IOException e1) {
                ShowMessage(e1.getMessage());
                return null; //TODO
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
            intentGet.putExtra("response", result);
            sendBroadcast(intentGet);
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
        handler.postDelayed(sendUpdatesToUI, ThermostatUtils.REFRESH_TIMEOUT * 1000);
    }


    private void sendDataToController(String data) {
        Log.d("Thermostat send command", data);
        new AsyncAction().execute(data);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            if (SetAllSettings) {
                SetAllSettings = false;
                sendDataToController("*" + ThermostatControllerData.Instance.encodeSettings());
            } else {
                sendDataToController(ThermostatControllerData.Instance.haveSettings() ? "?" : "@");
            }
            handler.postDelayed(this, ThermostatUtils.REFRESH_TIMEOUT * 1000);
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