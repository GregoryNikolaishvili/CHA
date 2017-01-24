package ge.altasoft.gia.cha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import ge.altasoft.gia.cha.thermostat.ThermostatBroadcastService;

public abstract class ChaActivity extends AppCompatActivity {

    final private BroadcastReceiver broadcastStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String statusMessage = intent.getStringExtra(MqttClient.MQTT_MSG);
            boolean isError = intent.getBooleanExtra(MqttClient.MQTT_MSG_IS_ERROR, false);

            if (isError)
                Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show();

            MqttClient.MQTTConnectionStatus status = (MqttClient.MQTTConnectionStatus) intent.getSerializableExtra(MqttClient.MQTT_CONN_STATUS);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                if (!isError)
                    actionBar.setSubtitle(statusMessage);
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                if (toolbar != null) {
                    switch (status) {
                        case INITIAL:
                            toolbar.setSubtitleTextColor(Color.DKGRAY);
                            break;
                        case CONNECTING:
                            toolbar.setSubtitleTextColor(Color.LTGRAY);
                            break;
                        case CONNECTED:
                            toolbar.setSubtitleTextColor(Color.WHITE);
                            break;
                        case NOTCONNECTED_UNKNOWNREASON:
                        case NOTCONNECTED_USERDISCONNECT:
                            toolbar.setSubtitleTextColor(Color.RED);
                            break;
                    }
                }
            }
        }
    };

    final private BroadcastReceiver broadcastDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MqttClient.MQTTReceivedDataType dataType = (MqttClient.MQTTReceivedDataType) intent.getSerializableExtra(MqttClient.MQTT_DATA_TYPE);
            processMqttData(dataType, intent);
        }
    };


    final private BroadcastReceiver thermostatBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int flags = intent.getIntExtra("result", 0);
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

    void processMqttData(MqttClient.MQTTReceivedDataType dataType, Intent intent) {
    }

    void processThermostatControllerData(int flags, Intent intent) {
    }
}
