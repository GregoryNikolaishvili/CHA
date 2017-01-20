package ge.altasoft.gia.cha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import ge.altasoft.gia.cha.thermostat.ThermostatBroadcastService;

public abstract class ChaActivity extends AppCompatActivity {

    final private BroadcastReceiver broadcastStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra(MqttClient.MQTT_MSG);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar!=null)
                actionBar.setSubtitle(status);
        }
    };

    final private BroadcastReceiver broadcastDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int what = intent.getIntExtra("what", Utils.FLAG_HAVE_NOTHING);
            processLightControllerData(what, intent);
        }
    };


    final private BroadcastReceiver thermostatBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int flags = intent.getIntExtra("result", Utils.FLAG_HAVE_NOTHING);
            processThermostatControllerData(flags, null);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(broadcastStatusReceiver, new IntentFilter(MqttClient.MQTT_STATUS_INTENT));
        registerReceiver(broadcastDataReceiver, new IntentFilter(MqttClient.MQTT_DATA_INTENT));
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(thermostatBroadcastReceiver, new IntentFilter(ThermostatBroadcastService.BROADCAST_ACTION_GET));
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(thermostatBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(broadcastDataReceiver);
        unregisterReceiver(broadcastStatusReceiver);
        super.onStop();
    }

    void processLightControllerData(int flags, Intent intent) {
    }

    void processThermostatControllerData(int flags, Intent intent) {
    }
}
