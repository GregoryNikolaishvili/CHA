package ge.altasoft.gia.cha.light;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.widget.BaseAdapter;

import ge.altasoft.gia.cha.ChaPreferenceActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.FriendlyEditTextPreference;
import ge.altasoft.gia.cha.views.TimePreference;

public class LightSettingsActivity extends ChaPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LightControllerData.Instance.saveToPreferences(prefs);

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

                            LightControllerData.Instance.decode(prefs);

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

            addPreferencesFromResource(R.xml.light_preferences);

            PreferenceScreen root = this.getPreferenceScreen();
            PreferenceCategory relays = (PreferenceCategory) root.findPreference("Relays");
            if (relays != null) {
                PreferenceManager prefMan = this.getPreferenceManager();
                Context prefContext = root.getContext();

                // Create the Preferences Manually - so that the key can be refresh programatically.

                for (int id = 1; id <= LightControllerData.RELAY_COUNT; id++) {
                    PreferenceScreen screen = prefMan.createPreferenceScreen(prefContext);

                    screen.setTitle(Integer.toString(id));

                    Preference p0 = new Preference(prefContext);
                    p0.setPersistent(false);
                    p0.setSelectable(false);
                    p0.setTitle("Relay #" + Integer.toString(id));
                    screen.addPreference(p0);

                    FriendlyEditTextPreference p1 = new FriendlyEditTextPreference(prefContext);
                    p1.setKey("l_relay_name_" + Integer.toString(id));
                    p1.setSummary("%s");
                    p1.setTitle("Name");
                    screen.addPreference(p1);

                    CheckBoxPreference p2 = new CheckBoxPreference(prefContext);
                    p2.setKey("l_is_active_" + Integer.toString(id));
                    p2.setTitle("Active");
                    screen.addPreference(p2);

                    TimePreference p3 = new TimePreference(prefContext);
                    p3.setKey("l_on_offset_" + Integer.toString(id));
                    p3.setTitle("On (offset to sunrise)");
                    screen.addPreference(p3);

                    ListPreference p4 = new ListPreference(prefContext);
                    p4.setDefaultValue("S");
                    p4.setEntries(R.array.listArray);
                    p4.setEntryValues(R.array.listValues);
                    p4.setKey("l_off_mode_" + Integer.toString(id));
                    p4.setSummary("%s");
                    p4.setTitle("Off mode");
                    screen.addPreference(p4);

                    TimePreference p5 = new TimePreference(prefContext);
                    p5.setKey("l_off_value_" + Integer.toString(id));
                    p5.setTitle("Value");
                    screen.addPreference(p5);
//
//                FriendlyEditTextPreference p6 = new FriendlyEditTextPreference(prefContext);
//                p6.setKey("l_sort_order_" + Integer.toString(id));
//                p6.setSummary("%s");
//                p6.setTitle("Sort order");
//                screen.addPreference(p1);

                    relays.addPreference(screen);
                }

                setRelayNamesAndSummary();
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
        }

        private void setRelayNamesAndSummary() {
            StringBuilder sb = new StringBuilder();

            PreferenceScreen root = getPreferenceScreen();
            PreferenceCategory relays = (PreferenceCategory) root.findPreference("Relays");
            if (relays != null) {
                for (int i = 0; i < relays.getPreferenceCount(); i++) {
                    if (relays.getPreference(i) instanceof PreferenceScreen) {
                        PreferenceScreen screen = (PreferenceScreen) relays.getPreference(i);
                        FriendlyEditTextPreference namePref = (FriendlyEditTextPreference) screen.getPreference(1);
                        CheckBoxPreference isActivePref = (CheckBoxPreference) screen.getPreference(2);

                        String name = namePref.getText();
                        if (name == null)
                            name = "Relay #" + Integer.toString(LightControllerData.Instance.relays(i).getId());
                        screen.setTitle(name);

                        if (isActivePref.isChecked()) {
                            sb.setLength(0);
                            //sb.append("Active, ");

                            TimePreference onOffset = (TimePreference) screen.getPreference(3);
                            ListPreference offMode = (ListPreference) screen.getPreference(4);
                            TimePreference offValue = (TimePreference) screen.getPreference(5);

                            sb.append("On: ");
                            if (onOffset.getValue() > 0) {
                                sb.append(onOffset.getDisplayTime());
                                sb.append(" after sunset");
                            } else if (onOffset.getValue() < 0) {
                                sb.append(onOffset.getDisplayTime((short) -onOffset.getValue()));
                                sb.append(" before sunset");
                            } else
                                sb.append(" at sunset");

                            sb.append(", Off: ");
                            switch (offMode.getValue()) {
                                case "S":
                                    if (offValue.getValue() > 0) {
                                        sb.append(offValue.getDisplayTime());
                                        sb.append(" after sunrise");
                                    } else if (offValue.getValue() < 0) {
                                        sb.append(offValue.getDisplayTime((short) -offValue.getValue()));
                                        sb.append(" before sunrise");
                                    } else
                                        sb.append(" at sunrise");
                                    break;
                                case "T": // positive values only
                                    sb.append(" at ");
                                    sb.append(offValue.getDisplayTime());
                                    break;
                                case "D": // positive values only
                                    sb.append(" after");
                                    sb.append(offValue.getDisplayTime());
                                    break;
                            }

                            screen.setSummary(sb.toString());
                        } else
                            screen.setSummary("");
                    }
                }
            }
            ((BaseAdapter) root.getRootAdapter()).notifyDataSetChanged();
        }
    }
}
