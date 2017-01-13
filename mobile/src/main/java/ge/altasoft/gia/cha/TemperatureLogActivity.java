package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.TemperaturePoint;
import ge.altasoft.gia.cha.thermostat.TemperaturePointArray;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class TemperatureLogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_log);

        TemperaturePointArray logBuffer = null;

        Intent intent = getIntent();
        String scope = intent.getStringExtra("scope");

        if (scope.equals("BoilerSensor")) {
            int id = intent.getIntExtra("id", 1);

            BoilerSensorData sensorData = ThermostatControllerData.Instance.boilerSensors(id);
            logBuffer = sensorData.getLogBuffer();
        }

        UsersAdapter adapter = new UsersAdapter(this, logBuffer);

        ListView listView = (ListView) findViewById(R.id.lvLog);
        listView.setAdapter(adapter);
    }

    public class UsersAdapter extends ArrayAdapter<TemperaturePoint> {
        public UsersAdapter(Context context, ArrayList<TemperaturePoint> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            TemperaturePoint user = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.temp_log_view, parent, false);
            }
            // Lookup view for data population
            TextView tvName = (TextView) convertView.findViewById(R.id.tvLogDateTime);
            TextView tvHome = (TextView) convertView.findViewById(R.id.tvLogValue);
            // Populate the data into the template view using the data object
            tvName.setText(user.first.toString());
            tvHome.setText(user.second.toString());

            // Return the completed view to render on screen
            return convertView;
        }
    }
}
