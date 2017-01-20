package ge.altasoft.gia.cha;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_preference);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final Activity context = this;
            Utils.ConfirmDialog(this, "Settings", "Save Settings?",
                    new Runnable() {
                        public void run() {
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                            Utils.mqttBrokerLocalUrl = prefs.getString("mtqq_url_local", Utils.mqttBrokerLocalUrl);
                            Utils.mqttBrokerGlobalUrl = prefs.getString("mtqq_url_global", Utils.mqttBrokerGlobalUrl);

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
