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
import ge.altasoft.gia.cha.classes.TempSensorData;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class LogTHActivity extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private THLogAdapter adapter = null;
    private boolean isTemperature = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_th);

        CircularArrayList<Pair<Date, Float>> logBuffer = null;

        Intent intent = getIntent();
        String scope = intent.getStringExtra("scope");

        switch (scope) {
            case "BoilerSensor": {
                int id = intent.getIntExtra("id", 0);

                if (id > 0) {
                    TempSensorData sensorData = ThermostatControllerData.Instance.boilerSensors(id - 1);
                    logBuffer = sensorData.getLogBuffer();
                }
                break;
            }
            case "RoomSensorT": {
                int id = intent.getIntExtra("id", 0);

                TempSensorData sensorData = ThermostatControllerData.Instance.roomSensors(id);
                logBuffer = sensorData.getLogBuffer();
                break;
            }
            case "RoomSensorH": {
                int id = intent.getIntExtra("id", 0);

                isTemperature = false;
                RoomSensorData sensorData = ThermostatControllerData.Instance.roomSensors(id);
                logBuffer = sensorData.getLogBufferH();
                break;
            }
        }

        if (logBuffer != null) {
            adapter = new THLogAdapter(this, logBuffer, isTemperature);

            ListView listView = (ListView) findViewById(R.id.lvLogTemperature);
            listView.setAdapter(adapter);
        }
    }

    public class THLogAdapter extends ArrayAdapter<Pair<Date, Float>> {
        final private boolean isTemperature;

        THLogAdapter(Context context, ArrayList<Pair<Date, Float>> points, boolean isTemperature) {
            super(context, 0, points);

            this.isTemperature = isTemperature;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.log_view_temperature, parent, false);
            }

            Pair<Date, Float> point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvLogDateTime)).setText(sdf.format(point.first));
                if (isTemperature)
                    ((TextView) convertView.findViewById(R.id.tvLogValue)).setText(String.format(Locale.US, "%.1f°", point.second));
                else
                    ((TextView) convertView.findViewById(R.id.tvLogValue)).setText(String.format(Locale.US, "%.0f %%", point.second));
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
