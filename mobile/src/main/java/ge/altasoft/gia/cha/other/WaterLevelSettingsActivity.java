package ge.altasoft.gia.cha.other;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import ge.altasoft.gia.cha.ChaPreferenceActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;

public class WaterLevelSettingsActivity extends ChaPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        OtherControllerData.Instance.saveWaterLevelToPreferences(prefs);

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
                            OtherControllerData.Instance.decodeWaterLevelSettings(prefs);

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

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.water_level_preferences);
        }
    }
}
