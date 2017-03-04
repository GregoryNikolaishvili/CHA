package ge.altasoft.gia.cha.thermostat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.widget.BaseAdapter;

import java.util.Locale;
import java.util.Map;

import ge.altasoft.gia.cha.ChaPreferenceActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.FriendlyEditTextPreference;
import ge.altasoft.gia.cha.views.TemperaturePreference;

public class ThermostatSettingsActivity extends ChaPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ThermostatControllerData.Instance.saveToPreferences(prefs);

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
            PreferenceCategory sensorsCat = (PreferenceCategory) root.findPreference("Sensors");
            if (sensorsCat != null) {
                PreferenceManager prefMan = this.getPreferenceManager();
                Context prefContext = root.getContext();

                Map<Integer, RoomSensorData> sensors = ThermostatControllerData.Instance.sortedRoomSensors();
                for (int id : sensors.keySet()) {
                    PreferenceScreen screen = prefMan.createPreferenceScreen(prefContext);

                    screen.setTitle(Integer.toString(id));

                    Preference p0 = new Preference(prefContext);
                    p0.setPersistent(false);
                    p0.setSelectable(false);
                    p0.setTitle("Sensor #" + Integer.toString(id));
                    p0.setKey("t_sensor_deleted_" + Integer.toString(id));
                    screen.addPreference(p0);

                    FriendlyEditTextPreference p1 = new FriendlyEditTextPreference(prefContext);
                    p1.setKey("t_sensor_name_" + Integer.toString(id));
                    p1.setSummary("%s");
                    p1.setTitle("Name");
                    screen.addPreference(p1);

                    TemperaturePreference p2 = new TemperaturePreference(prefContext);
                    p2.setKey("t_target_t_" + Integer.toString(id));
                    p2.setSummary("%s°");
                    p2.setTitle("Target temperature");
                    screen.addPreference(p2);

                    FriendlyEditTextPreference p3 = new FriendlyEditTextPreference(prefContext);
                    p3.setKey("t_resp_relay_id_" + Integer.toString(id));
                    p3.setSummary("# %s");
                    p3.setTitle("Responsible relay id");
                    screen.addPreference(p3);

                    Preference p4 = new Preference(prefContext);
                    p4.setKey(p0.getTitle().toString());
                    p4.setTitle("Delete");
                    p4.setSummary("Press to delete this sensor");
                    screen.addPreference(p4);

                    p4.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {

                            final Preference _preference = preference;
                            Utils.ConfirmDialog(_preference.getContext(), "Delete", "Are you sure you want to delete this sensor?",
                                    new Runnable() {
                                        public void run() {
                                            String key = _preference.getKey();

                                            PreferenceScreen root = getPreferenceScreen();
                                            PreferenceCategory sensors = (PreferenceCategory) root.findPreference("Sensors");
                                            if (sensors != null) {
                                                for (int i = 0; i < sensors.getPreferenceCount(); i++) {
                                                    if (sensors.getPreference(i) instanceof PreferenceScreen) {
                                                        PreferenceScreen screen = (PreferenceScreen) sensors.getPreference(i);
                                                        Preference pref0 = screen.getPreference(0);

                                                        if (pref0.getTitle().toString().equals(key)) {
                                                            SharedPreferences prefs = _preference.getSharedPreferences();
                                                            SharedPreferences.Editor editor = prefs.edit();
                                                            editor.putBoolean("t_sensor_deleted_".concat(key), true);
                                                            editor.apply();

                                                            pref0.setTitle(pref0.getTitle().toString().concat(" - deleted"));
                                                            screen.setSummary("Deleted");
                                                            screen.setEnabled(false);

                                                            setSensorNamesAndSummary();
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    null
                            );

                            return true;
                        }
                    });

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

            setSensorNamesAndSummary();
        }

        private void setSensorNamesAndSummary() {
            PreferenceScreen root = getPreferenceScreen();
            PreferenceCategory sensors = (PreferenceCategory) root.findPreference("Sensors");
            if (sensors != null) {
                for (int i = 0; i < sensors.getPreferenceCount(); i++) {
                    if (sensors.getPreference(i) instanceof PreferenceScreen) {
                        PreferenceScreen screen = (PreferenceScreen) sensors.getPreference(i);
                        FriendlyEditTextPreference namePref = (FriendlyEditTextPreference) screen.getPreference(1);
                        TemperaturePreference targetTPref = (TemperaturePreference) screen.getPreference(2);
                        FriendlyEditTextPreference relayPref = (FriendlyEditTextPreference) screen.getPreference(3);

                        screen.setTitle(namePref.getText() == null ? "Sensor #" + Integer.toString(ThermostatControllerData.Instance.relays(i).getId()) : namePref.getText());

                        String summary = "";
                        if (screen.isEnabled()) {
                            if (targetTPref.getText() != null)
                                summary = String.format(Locale.US, "Set T: %s°", targetTPref.getText());
                            if ((relayPref.getText() != null) && (!relayPref.getText().equals("0"))) {
                                if (summary.length() > 0)
                                    summary = summary.concat(", ");
                                summary = summary.concat(String.format(Locale.US, "Heater Relay #%s", relayPref.getText()));
                            }
                        } else
                            summary = "Deleted";
                        screen.setSummary(summary);
                    }
                }
            }
            ((BaseAdapter) root.getRootAdapter()).notifyDataSetChanged();
        }
    }
}
