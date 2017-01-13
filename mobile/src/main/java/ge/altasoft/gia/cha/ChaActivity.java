package ge.altasoft.gia.cha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;

import ge.altasoft.gia.cha.light.LightBroadcastService;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatBroadcastService;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public abstract class ChaActivity extends AppCompatActivity {

    private BroadcastReceiver lightBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("response");
            processLightControllerData(response);
        }
    };

    private BroadcastReceiver thermostatBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("response");
            processThermostatControllerData(response);
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(lightBroadcastReceiver, new IntentFilter(LightBroadcastService.BROADCAST_ACTION_GET));
        registerReceiver(thermostatBroadcastReceiver, new IntentFilter(ThermostatBroadcastService.BROADCAST_ACTION_GET));
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(thermostatBroadcastReceiver);
        unregisterReceiver(lightBroadcastReceiver);
    }

    protected int processLightControllerData(String response) {
        return LightControllerData.Instance.decode(response);
    }

    protected int processThermostatControllerData(String response)
    {
        return ThermostatControllerData.Instance.decode(response);
    }
}
