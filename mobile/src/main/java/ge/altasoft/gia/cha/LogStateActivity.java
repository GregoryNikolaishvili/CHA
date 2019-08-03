package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.classes.LogOneValueItem;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.other.OtherControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class LogStateActivity extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
    private WidgetType scope;
    private int relayId;

    private StateLogAdapter adapter = null;
    private ArrayList<LogOneValueItem> logBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_state);

        Intent intent = getIntent();
        scope = (WidgetType) intent.getSerializableExtra("widget");
        relayId = intent.getIntExtra("id", -1);

        logBuffer = new ArrayList<>();
        adapter = new StateLogAdapter(this, logBuffer);

        ListView listView = (ListView) findViewById(R.id.lvLogState);
        listView.setAdapter(adapter);
    }

    private void RequestLog(int wd) {
        switch (scope) {
            case BoilerPump:
                publish("cha/hub/getlog", "brelay_".concat(String.valueOf(wd)), false);
                break;
            case LightRelay:
                publish("cha/hub/getlog", "light_".concat(String.valueOf(wd)), false);
                break;
        }
    }

    @Override
    protected void ServiceConnected() {
        super.ServiceConnected();

        RequestLog(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1);
    }

    @Override
    public void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        int id;
        RelayData data;

        switch (dataType) {
            case ThermostatBoilerPumpState:
                if (scope != WidgetType.BoilerPump)
                    return;
                id = intent.getIntExtra("id", -1);
                if (id != relayId)
                    return;

                data = ThermostatControllerData.Instance.boilerPumps(id);

                LogOneValueItem point = new LogOneValueItem(new Date(data.getLastSyncTime()), data.getState());
                logBuffer.add(point);
                adapter.notifyDataSetChanged();

                break;

            case LightRelayState:
                if (scope != WidgetType.LightRelay)
                    return;
                id = intent.getIntExtra("id", -1);
                if (id != relayId)
                    return;

                data = LightControllerData.Instance.relays(id);

                LogOneValueItem point2 = new LogOneValueItem(new Date(data.getLastSyncTime()), data.getState());
                logBuffer.add(point2);
                adapter.notifyDataSetChanged();

                break;

            case WaterLevelRelayState:
                if (scope != WidgetType.WaterLevelPumpRelay)
                    return;
                id = intent.getIntExtra("id", -1);
                if (id != relayId)
                    return;

                data = OtherControllerData.Instance.relays(id);

                LogOneValueItem point3 = new LogOneValueItem(new Date(data.getLastSyncTime()), data.getState());
                logBuffer.add(point3);
                adapter.notifyDataSetChanged();

                break;

            case Log:
                switch (scope) {
                    case BoilerPump:
                        if (intent.getStringExtra("type").startsWith("brelay")) {
                            String log = intent.getStringExtra("log");
                            FillRelayLog(relayId, log, logBuffer);
                            adapter.notifyDataSetChanged();
                        }
                        break;

                    case LightRelay:
                        if (intent.getStringExtra("type").startsWith("light")) {
                            String log = intent.getStringExtra("log");
                            FillRelayLog(relayId, log, logBuffer);
                            adapter.notifyDataSetChanged();
                        }
                        break;

                    case WaterLevelPumpRelay:
                        if (intent.getStringExtra("type").startsWith("trelay")) {
                            String log = intent.getStringExtra("log");
                            FillRelayLog(relayId, log, logBuffer);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                }
                break;
        }
    }

    class StateLogAdapter extends ArrayAdapter<LogOneValueItem> {
        StateLogAdapter(Context context, ArrayList<LogOneValueItem> points) {
            super(context, 0, points);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.log_view_boolean, parent, false);
            }

            LogOneValueItem point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText(sdf.format(point.date));
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue)).setText(point.state == 0 ? "Off" : (point.state == 1 ? "On" : "Pending on"));
            }
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);

        int id = 0;
        int logSuffix = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        switch (logSuffix) {
            case 0:
                id = R.id.action_sunday;
                break;
            case 1:
                id = R.id.action_monday;
                break;
            case 2:
                id = R.id.action_tuesday;
                break;
            case 3:
                id = R.id.action_wednesday;
                break;
            case 4:
                id = R.id.action_thursday;
                break;
            case 5:
                id = R.id.action_friday;
                break;
            case 6:
                id = R.id.action_saturday;
                break;
        }
        menu.findItem(id).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        item.setChecked(!item.isChecked());

        int wd = -1;
        switch (id) {
            case R.id.action_sunday:
                wd = 0;
                break;
            case R.id.action_monday:
                wd = 1;
                break;
            case R.id.action_tuesday:
                wd = 2;
                break;
            case R.id.action_wednesday:
                wd = 3;
                break;
            case R.id.action_thursday:
                wd = 4;
                break;
            case R.id.action_friday:
                wd = 5;
                break;
            case R.id.action_saturday:
                wd = 6;
                break;
        }

        if (wd >= 0) {
            RequestLog(wd);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static void FillRelayLog(int relayId, String log, ArrayList<LogOneValueItem> logBuffer) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.US);
        String date0 = sdf.format(new Date());
        sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);

        logBuffer.clear();

        int id;
        Date XX;
        int state;


        String[] logEntries = log.split(":");
        for (String logEntry : logEntries) {
            if (logEntry.length() == 8) {
                try {
                    XX = sdf.parse(date0 + logEntry.substring(0, 6));
                } catch (ParseException ex) {
                    Log.e("Log", "Invalid X", ex);
                    continue;
                }

                try {
                    id = Integer.parseInt(logEntry.substring(6, 7), 16);
                } catch (NumberFormatException ex) {
                    Log.e("Log", "Invalid id", ex);
                    continue;
                }

                if (id != relayId)
                    continue;

                try {
                    state = Integer.parseInt(logEntry.substring(7, 8), 16);
                } catch (NumberFormatException ex) {
                    Log.e("Log", "Invalid Y", ex);
                    continue;
                }

                logBuffer.add(new LogOneValueItem(XX, state));
            }
        }
    }

}
