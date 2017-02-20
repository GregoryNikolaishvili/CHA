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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class LogBooleanActivity extends ChaActivity {

    public class LogItem {
        public Date date;
        public int state;

        public LogItem(Date date, int state) {
            this.date = date;
            this.state = state;
        }
    }

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private String scope;
    private int sensorId;

    private StateLogAdapter adapter = null;
    private ArrayList<LogItem> logBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_boolean);

        Intent intent = getIntent();
        String scope = intent.getStringExtra("scope");
        sensorId = intent.getIntExtra("id", -1);

        switch (scope) {
            case "LightRelay": {
                int id = intent.getIntExtra("id", 0);

                if (id > 0) {
                    RelayData sensorData = LightControllerData.Instance.relays(id);
                    //logBuffer = sensorData.getLogBuffer();
                }
                break;
            }
        }

//        if (logBuffer != null) {
//            adapter = new StateLogAdapter(this, logBuffer);
//
//            ListView listView = (ListView) findViewById(R.id.lvLogBoolean);
//            listView.setAdapter(adapter);
//        }
    }

    @Override
    protected void ServiceConnected() {
        super.ServiceConnected();

        switch (scope) {
            case "BoilerPump": {
                publish("cha/hub/getlog", "brelay_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
                break;
            }
            case "LightRelay": {
                publish("cha/hub/getlog", "light_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
                break;
            }
        }
    }

    @Override
    public void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        switch (dataType) {
            case ThermostatBoilerPumpState:
                if (!scope.equals("BoilerPump"))
                    return;
                int id = intent.getIntExtra("id", 0);
                if (id != sensorId)
                    return;

                RelayData data = ThermostatControllerData.Instance.boilerPumps(id);

                LogItem point = new LogItem(new Date(data.getLastReadingTime()), data.isOn() ? 1 : 0);
                logBuffer.add(point);
                adapter.notifyDataSetChanged();

                break;

            case LightRelayState:
                if (!scope.equals("LightRelay"))
                    return;
                int id2 = intent.getIntExtra("id", 0);
                if (id2 != sensorId)
                    return;

                RelayData data2 = LightControllerData.Instance.relays(id2);

                LogItem point2 = new LogItem(new Date(data2.getLastReadingTime()), data2.isOn() ? 1 : 0);
                logBuffer.add(point2);
                adapter.notifyDataSetChanged();

                break;

            case ThermostatLog:
                //case LightLog: // TODO: 2/20/2017
                switch (scope) {
                    case "BoilerPump":
                        if (intent.getStringExtra("type").startsWith("brelay")) {
                            String log = intent.getStringExtra("log");
                            // TODO: 2/20/2017
                            //ThermostatUtils.FillSensorLog(sensorId, scope, log, logBuffer);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    case "LightRelay":
                        if (intent.getStringExtra("type").startsWith("light")) {
                            String log = intent.getStringExtra("log");
                            // TODO: 2/20/2017
                            //ThermostatUtils.FillSensorLog(sensorId, scope, log, logBuffer);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                }
                break;
        }
    }

    public class StateLogAdapter extends ArrayAdapter<Pair<Date, Integer>> {
        StateLogAdapter(Context context, ArrayList<Pair<Date, Integer>> points) {
            super(context, 0, points);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.log_view_boolean, parent, false);
            }

            Pair<Date, Integer> point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText(sdf.format(point.first));
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue)).setText(point.second == 0 ? "Off" : (point.second == 1 ? "On" : "Pending on"));
            }
            return convertView;
        }
    }
}
