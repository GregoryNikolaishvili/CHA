package ge.altasoft.gia.cha.thermostat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.widget.BaseAdapter;

import java.util.Map;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.FriendlyEditTextPreference;
import ge.altasoft.gia.cha.views.TemperaturePreference;

public class ThermostatSettingsActivity extends PreferenceActivity {

    PrefsFragment prefsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ThermostatControllerData.Instance.saveToPreferences(prefs);

        prefsFragment = new PrefsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final Activity context = this;
            Utils.ConfirmDialog(this, "Settings", "Save Settings?",
                    new Runnable() {
                        public void run() {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                            ThermostatControllerData.Instance.decode(prefs);

                            //Log.d("Settings refresh", ThermostatControllerData.Instance.Encode());

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

    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.thermostat_preferences);

            PreferenceScreen root = this.getPreferenceScreen();
            PreferenceCategory relaysCat = (PreferenceCategory) root.findPreference("Relays");
            if (relaysCat != null) {
                PreferenceManager prefMan = this.getPreferenceManager();
                Context prefContext = root.getContext();

                // Create the Preferences Manually - so that the key can be refresh programatically.

                for (int id = 1; id <= ThermostatControllerData.RELAY_COUNT; id++) {
                    PreferenceScreen screen = prefMan.createPreferenceScreen(prefContext);

                    screen.setTitle(Integer.toString(id));

                    Preference p0 = new Preference(prefContext);
                    p0.setPersistent(false);
                    p0.setSelectable(false);
                    p0.setTitle("Relay #" + Integer.toString(id));
                    screen.addPreference(p0);

                    FriendlyEditTextPreference p1 = new FriendlyEditTextPreference(prefContext);
                    p1.setKey("t_relay_name_" + Integer.toString(id));
                    p1.setSummary("%s");
                    p1.setTitle("Name");
                    screen.addPreference(p1);

                    relaysCat.addPreference(screen);
                }

                setRelayNamesAndSummary();
            }

            PreferenceCategory sensorsCat = (PreferenceCategory) root.findPreference("Sensors");
            if (sensorsCat != null) {
                PreferenceManager prefMan = this.getPreferenceManager();
                Context prefContext = root.getContext();

                Map<Integer, RoomSensorData> sensors = ThermostatControllerData.Instance.sortedRoomSensors();
                for (int id : sensors.keySet()) {
                    RoomSensorData data = ThermostatControllerData.Instance.roomSensors(id);

                    PreferenceScreen screen = prefMan.createPreferenceScreen(prefContext);

                    screen.setTitle(Integer.toString(id));

                    Preference p0 = new Preference(prefContext);
                    p0.setPersistent(false);
                    p0.setSelectable(false);
                    p0.setTitle("Sensor #" + Integer.toString(id));
                    screen.addPreference(p0);

                    FriendlyEditTextPreference p1 = new FriendlyEditTextPreference(prefContext);
                    p1.setKey("t_sensor_name_" + Integer.toString(id));
                    p1.setSummary("%s");
                    p1.setTitle("Name");
                    screen.addPreference(p1);

                    TemperaturePreference p2 = new TemperaturePreference(prefContext);
                    p2.setKey("t_desired_t_" + Integer.toString(id));
                    p2.setSummary("%s°");
                    p2.setTitle("Desired temperature");
                    screen.addPreference(p2);

                    sensorsCat.addPreference(screen);
                }

                setSensorNamesAndSummary();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            setRelayNamesAndSummary();
            setSensorNamesAndSummary();
        }

        private void setRelayNamesAndSummary() {
            PreferenceScreen root = getPreferenceScreen();
            PreferenceCategory relays = (PreferenceCategory) root.findPreference("Relays");
            if (relays != null) {
                for (int i = 0; i < relays.getPreferenceCount(); i++) {
                    if (relays.getPreference(i) instanceof PreferenceScreen) {
                        PreferenceScreen screen = (PreferenceScreen) relays.getPreference(i);
                        FriendlyEditTextPreference namePref = (FriendlyEditTextPreference) screen.getPreference(1);

                        String name = namePref.getText();
                        if (name == null)
                            name = "Relay #" + Integer.toString(ThermostatControllerData.Instance.relays(i).getId());
                        screen.setTitle(name);
                    }
                }
            }
            ((BaseAdapter) root.getRootAdapter()).notifyDataSetChanged();
        }

        private void setSensorNamesAndSummary() {
            PreferenceScreen root = getPreferenceScreen();
            PreferenceCategory sensors = (PreferenceCategory) root.findPreference("Sensors");
            if (sensors != null) {
                for (int i = 0; i < sensors.getPreferenceCount(); i++) {
                    if (sensors.getPreference(i) instanceof PreferenceScreen) {
                        PreferenceScreen screen = (PreferenceScreen) sensors.getPreference(i);
                        FriendlyEditTextPreference namePref = (FriendlyEditTextPreference) screen.getPreference(1);

                        String name = namePref.getText();
                        if (name == null)
                            name = "Sensor #" + Integer.toString(ThermostatControllerData.Instance.relays(i).getId());
                        screen.setTitle(name);
                    }
                }
            }
            ((BaseAdapter) root.getRootAdapter()).notifyDataSetChanged();
        }
    }
}
