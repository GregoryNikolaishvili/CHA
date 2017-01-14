package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.TemperaturePoint;
import ge.altasoft.gia.cha.thermostat.TemperaturePointArray;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class TemperatureLogActivity extends ChaActivity {

    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private TemperatureLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_log);

        TemperaturePointArray logBuffer = null;

        Intent intent = getIntent();
        String scope = intent.getStringExtra("scope");

        if (scope.equals("BoilerSensor")) {
            int id = intent.getIntExtra("id", 0);

            if (id > 0) {
                BoilerSensorData sensorData = ThermostatControllerData.Instance.boilerSensors(id - 1);
                logBuffer = sensorData.getLogBuffer();
            }
        }

        if (logBuffer != null) {
            adapter = new TemperatureLogAdapter(this, logBuffer);

            ListView listView = (ListView) findViewById(R.id.lvLog);
            listView.setAdapter(adapter);
        }
    }

    public class TemperatureLogAdapter extends ArrayAdapter<TemperaturePoint> {
        public TemperatureLogAdapter(Context context, ArrayList<TemperaturePoint> points) {
            super(context, 0, points);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TemperaturePoint point = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.temp_log_view, parent, false);
            }

            TextView tvName = (TextView) convertView.findViewById(R.id.tvLogDateTime);
            TextView tvHome = (TextView) convertView.findViewById(R.id.tvLogValue);

            tvName.setText(sdf.format(point.first).toString());
            tvHome.setText(String.format(Locale.US, "%.1f°", point.second));

            return convertView;
        }
    }

    @Override
    protected void processThermostatControllerData(int flags) {
        super.processThermostatControllerData(flags);

        if ((flags & Utils.FLAG_HAVE_STATE) != 0) {
            adapter.notifyDataSetChanged();
        }
    }
}
