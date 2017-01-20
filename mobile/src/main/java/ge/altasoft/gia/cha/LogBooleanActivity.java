package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.classes.CircularArrayList;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class LogBooleanActivity extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private BooleanLogAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_boolean);

        CircularArrayList<Pair<Date, Boolean>> logBuffer = null;

        Intent intent = getIntent();
        String scope = intent.getStringExtra("scope");

        switch (scope) {
            case "BoilerPump": {
                int id = intent.getIntExtra("id", -1);

                if (id >= 0) {
                    RelayData sensorData = ThermostatControllerData.Instance.boilerPumps(id);
                    logBuffer = sensorData.getLogBuffer();
                }
                break;
            }
            case "ThermostatRelay": {
                int id = intent.getIntExtra("id", 0);

                if (id > 0) {
                    RelayData sensorData = ThermostatControllerData.Instance.relays(id - 1);
                    logBuffer = sensorData.getLogBuffer();
                }
                break;
            }
            case "LightRelay": {
                int id = intent.getIntExtra("id", 0);

                if (id > 0) {
                    RelayData sensorData = LightControllerData.Instance.relays(id - 1);
                    logBuffer = sensorData.getLogBuffer();
                }
                break;
            }
        }

        if (logBuffer != null) {
            adapter = new BooleanLogAdapter(this, logBuffer);

            ListView listView = (ListView) findViewById(R.id.lvLogBoolean);
            listView.setAdapter(adapter);
        }
    }

    public class BooleanLogAdapter extends ArrayAdapter<Pair<Date, Boolean>> {
        BooleanLogAdapter(Context context, ArrayList<Pair<Date, Boolean>> points) {
            super(context, 0, points);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.log_view_boolean, parent, false);
            }

            Pair<Date, Boolean> point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvLogDateTime)).setText(sdf.format(point.first));
                ((TextView) convertView.findViewById(R.id.tvLogValue)).setText(point.second ? "On" : "Off");
            }
            return convertView;
        }
    }

    @Override
    protected void processThermostatControllerData(int flags, Intent intent) {
        super.processThermostatControllerData(flags, intent);

        if ((flags & Utils.FLAG_HAVE_STATE) != 0) {
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }
}
