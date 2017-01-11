package ge.altasoft.gia.cha;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import ge.altasoft.gia.cha.light.LightUtils;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main_preference);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final Activity context = this;
            Utils.ConfirmDialog(this, "Settings", "Save Settings?",
                    new Runnable() {
                        public void run() {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                            LightUtils.LocalIP = prefs.getString("light_controller_ip", LightUtils.LocalIP);
                            LightUtils.REFRESH_TIMEOUT = prefs.getInt("light_controller_polling", LightUtils.REFRESH_TIMEOUT);

                            ThermostatUtils.LocalIP = prefs.getString("thermostat_controller_ip", ThermostatUtils.LocalIP);
                            ThermostatUtils.REFRESH_TIMEOUT = prefs.getInt("thermostat_controller_polling", ThermostatUtils.REFRESH_TIMEOUT);

                            setResult(Activity.RESULT_OK, null); //The data you want to send back
                            context.finish();
                        }
                    },
                    new Runnable() {
                        public void run() {
                            context.finish();
                        }
                    }
            );
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
