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

import ge.altasoft.gia.cha.classes.LogRelayItem;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class LogStateActivity extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private String scope;
    private int relayId;

    private StateLogAdapter adapter = null;
    private ArrayList<LogRelayItem> logBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_state);

        Intent intent = getIntent();
        scope = intent.getStringExtra("scope");
        relayId = intent.getIntExtra("id", -1);

        logBuffer = new ArrayList<>();
        adapter = new StateLogAdapter(this, logBuffer);

        ListView listView = (ListView) findViewById(R.id.lvLogState);
        listView.setAdapter(adapter);
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
                if (id != relayId)
                    return;

                RelayData data = ThermostatControllerData.Instance.boilerPumps(id);

                LogRelayItem point = new LogRelayItem(new Date(data.getLastSyncTime()), data.getState());
                logBuffer.add(point);
                adapter.notifyDataSetChanged();

                break;

            case LightRelayState:
                if (!scope.equals("LightRelay"))
                    return;
                int id2 = intent.getIntExtra("id", 0);
                if (id2 != relayId)
                    return;

                RelayData data2 = LightControllerData.Instance.relays(id2);

                LogRelayItem point2 = new LogRelayItem(new Date(data2.getLastSyncTime()), data2.getState());
                logBuffer.add(point2);
                adapter.notifyDataSetChanged();

                break;

            case Log:
                switch (scope) {
                    case "BoilerPump":
                        if (intent.getStringExtra("type").startsWith("brelay")) {
                            String log = intent.getStringExtra("log");

                            ThermostatUtils.FillRelayLog(relayId, scope, log, logBuffer);
                            adapter.notifyDataSetChanged();
                        }
                        break;

                    case "LightRelay":

                        if (intent.getStringExtra("type").startsWith("light")) {
                            String log = intent.getStringExtra("log");

                            ThermostatUtils.FillRelayLog(relayId, scope, log, logBuffer);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                }
                break;
        }
    }

    public class StateLogAdapter extends ArrayAdapter<LogRelayItem> {
        StateLogAdapter(Context context, ArrayList<LogRelayItem> points) {
            super(context, 0, points);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.log_view_boolean, parent, false);
            }

            LogRelayItem point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText(sdf.format(point.date));
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue)).setText(point.state == 0 ? "Off" : (point.state == 1 ? "On" : "Pending on"));
            }
            return convertView;
        }
    }
}
