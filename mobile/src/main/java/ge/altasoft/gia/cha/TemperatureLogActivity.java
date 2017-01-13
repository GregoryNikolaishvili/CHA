package ge.altasoft.gia.cha;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class TemperatureLogActivity extends AppCompatActivity {

    private static final String TAG_DATE_TIME = "DateTime";
    private static final String TAG_VALUE = "Value";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_log);

        CircularArrayList<Pair<Date, Double>> logBuffer = null;

        Intent intent = getIntent();
        String scope = intent.getStringExtra("scope");

        if (scope.equals("BoilerSensor")) {
            int id = intent.getIntExtra("id", 1);

            BoilerSensorData sensorData = ThermostatControllerData.Instance.boilerSensors(id);
            logBuffer = sensorData.getLogBuffer();
        }

        ArrayList<HashMap<String, String>> oslist;

        // Adding value HashMap key => value
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TAG_DATE_TIME, datetime);
        map.put(TAG_VALUE, ipAddress);
        oslist.add(map);

        ListView list = (ListView) findViewById(R.id.lvLog);

        ListAdapter adapter = new SimpleAdapter(TemperatureLogActivity.this, oslist,
                R.layout.temp_log_view,
                new String[]{TAG_DATE_TIME, TAG_VALUE},
                new int[]{R.id.tvLogDateTime, R.id.tvLogValue}
        );

        list.setAdapter(adapter);

    }
}
