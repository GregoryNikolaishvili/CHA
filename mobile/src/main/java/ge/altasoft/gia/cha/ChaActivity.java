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
            int flags = intent.getIntExtra("result", Utils.FLAG_HAVE_NOTHING);
            processLightControllerData(flags);
        }
    };

    private BroadcastReceiver thermostatBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int flags = intent.getIntExtra("result", Utils.FLAG_HAVE_NOTHING);
            processThermostatControllerData(flags);
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

    protected void processLightControllerData(int flags) {
    }

    protected void processThermostatControllerData(int flags) {
    }
}
