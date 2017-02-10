package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.LogItem;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class LogTHActivity extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private THLogAdapter adapter = null;
    private String scope;
    private int sensorId;
    private ArrayList<LogItem> logBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_th);

        Intent intent = getIntent();
        scope = intent.getStringExtra("scope");
        sensorId = intent.getIntExtra("id", 0);

        logBuffer = new ArrayList<>();
        adapter = new THLogAdapter(this, logBuffer, scope.equals("RoomSensor"));

        ListView listView = (ListView) findViewById(R.id.lvLogTemperatureAndHumidity);
        listView.setAdapter(adapter);
    }

    @Override
    protected void ServiceConnected() {
        super.ServiceConnected();

        switch (scope) {
            case "BoilerSensor": {
                publish("cha/hub/getlog", "boiler_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
                break;
            }
            case "RoomSensor": {
                publish("cha/hub/getlog", "room_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
                break;
            }
        }
    }

    @Override
    void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        switch (dataType) {
            case ThermostatBoilerSensorState:
                if (!scope.equals("BoilerSensor"))
                    return;
                int id = intent.getIntExtra("id", 0);
                if (id != sensorId)
                    return;

                BoilerSensorData data = ThermostatControllerData.Instance.boilerSensors(id);
                float v = data.getTemperature();
                if (!Float.isNaN(v)) {
                    LogItem point = new LogItem(new Date(data.getLastReadingTime()), v, 0f);
                    logBuffer.add(point);
                    adapter.notifyDataSetChanged();
                }
                break;

            case ThermostatLog:
                switch (scope) {
                    case "BoilerSensor":
                        if (intent.getStringExtra("type").startsWith("boiler"))
                            rebuildLog(intent.getStringExtra("log"));
                        break;
                    case "RoomSensor":
                        if (intent.getStringExtra("type").startsWith("room"))
                            rebuildLog(intent.getStringExtra("log"));
                        break;
                }
                break;
        }
    }

    public void rebuildLog(String log) {

        ThermostatUtils.FillSensorLog(log, logBuffer, sensorId, scope);
        adapter.notifyDataSetChanged();
    }

    public class THLogAdapter extends ArrayAdapter<LogItem> {

        private boolean hasHumidity;

        THLogAdapter(Context context, ArrayList<LogItem> points, boolean hasHumidity) {
            super(context, 0, points);

            this.hasHumidity = hasHumidity;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_key_value, parent, false);
            }

            LogItem point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText(sdf.format(point.date));
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue1)).setText(String.format(Locale.US, "%.1f°", point.T));

                if (hasHumidity)
                    ((TextView) convertView.findViewById(R.id.tvListViewItemValue2)).setText(String.format(Locale.US, "%.0f %%", point.H));
                else
                    ((TextView) convertView.findViewById(R.id.tvListViewItemValue2)).setVisibility(View.GONE);
            }
            return convertView;
        }
    }
}
