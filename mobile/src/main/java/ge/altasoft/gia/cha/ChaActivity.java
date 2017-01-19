package ge.altasoft.gia.cha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import ge.altasoft.gia.cha.thermostat.ThermostatBroadcastService;

public abstract class ChaActivity extends AppCompatActivity {

    private BroadcastReceiver broadcastStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(MqttClient.MQTT_MSG);
            getSupportActionBar().setSubtitle(status);
        }
    };

    private BroadcastReceiver broadcastDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int what = intent.getIntExtra("what", Utils.FLAG_HAVE_NOTHING);
            processLightControllerData(what, intent);
        }
    };


    private BroadcastReceiver thermostatBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int flags = intent.getIntExtra("result", Utils.FLAG_HAVE_NOTHING);
            processThermostatControllerData(flags, null);
        }
    };


    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(MqttClient.MQTT_STATUS_INTENT);
        registerReceiver(broadcastStatusReceiver, intentFilter);

        intentFilter = new IntentFilter(MqttClient.MQTT_DATA_INTENT);
        registerReceiver(broadcastDataReceiver, intentFilter);


        registerReceiver(thermostatBroadcastReceiver, new IntentFilter(ThermostatBroadcastService.BROADCAST_ACTION_GET));
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(thermostatBroadcastReceiver);
    }

    protected void processLightControllerData(int flags, Intent intent) {
    }

    protected void processThermostatControllerData(int flags, Intent intent) {
    }
}
