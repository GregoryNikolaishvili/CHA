package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ge.altasoft.gia.cha.classes.LogTwoValueItem;
import ge.altasoft.gia.cha.classes.WidgetType;

public class LogActivity2 extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd HH:mm:ss", Locale.US);
    final private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.US);
    final private SimpleDateFormat sdfX = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
    final private SimpleDateFormat sdf0 = new SimpleDateFormat("yyMMdd", Locale.US);

    private Date startTime;

    private int endWeekDay;
    private int startWeekDay;
    private String startDate0;
    private String endDate0;

    private WidgetType scope;
    private int widgetId;

    private _5in1LogAdapter adapter = null;
    private ArrayList<LogTwoValueItem> logBuffer;

    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log2);

        Intent intent = getIntent();
        scope = (WidgetType) intent.getSerializableExtra("widget");
        widgetId = intent.getIntExtra("id", -1);

        logBuffer = new ArrayList<>();
        adapter = new _5in1LogAdapter(this, logBuffer,
                (scope == WidgetType.WindSensor) || (scope == WidgetType.WindDirSensor) || (scope == WidgetType.WaterLevelSensor) || (scope == WidgetType.RainSensor));

        ListView listView = (ListView) findViewById(R.id.lvLog);
        listView.setAdapter(adapter);

        chart = (LineChart) findViewById(R.id.chart);
        chart.getLegend().setEnabled(false);
        chart.setNoDataText("Loading ...");

        XAxis x = chart.getXAxis();
        x.setLabelCount(6, false);
        x.setTextColor(Color.WHITE);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(true);
        x.setAxisLineColor(Color.WHITE);
        x.setValueFormatter(new TimeValueFormatter());

        YAxis y = chart.getAxisLeft();
        y.setLabelCount(6, false);
        y.setTextColor(Color.WHITE);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(true);
        y.setAxisLineColor(Color.WHITE);

        chart.getAxisRight().setEnabled(false);

        SetStartAndDates(-1, false);
    }

    private void RequestLog(int wd) {
        switch (scope) {
            case WindSensor:
            case WindDirSensor:
                publish("cha/hub/getlog", "wind_".concat(String.valueOf(wd)), false);
            case RainSensor:
                publish("cha/hub/getlog", "rain_".concat(String.valueOf(wd)), false);
            case PressureSensor:
                publish("cha/hub/getlog", "pres_".concat(String.valueOf(wd)), false);
                break;
            case WaterLevelSensor:
                publish("cha/hub/getlog", "tank_".concat(String.valueOf(wd)), false);
                break;
        }
    }

    @Override
    protected void ServiceConnected() {
        super.ServiceConnected();

        logBuffer.clear();

        RequestLog(startWeekDay);
        RequestLog(endWeekDay);
    }

    @Override
    public void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        if (dataType == MqttClientLocal.MQTTReceivedDataType.Log) {
            switch (scope) {
                case WindSensor:
                case WindDirSensor:
                    String type = intent.getStringExtra("type");
                    if (intent.getStringExtra("type").startsWith("wind_")) {
                        String log = intent.getStringExtra("log");
                        int wd = Integer.parseInt(type.substring(5));
                        if (Fill5in1SensorLog(wd, scope, log, logBuffer)) {
                            adapter.notifyDataSetChanged();
                            DrawChart(logBuffer, chart);
                        }
                    }
                    break;
                case RainSensor:
                    type = intent.getStringExtra("type");
                    if (intent.getStringExtra("type").startsWith("rain_")) {
                        String log = intent.getStringExtra("log");
                        int wd = Integer.parseInt(type.substring(5));
                        if (Fill5in1SensorLog(wd, scope, log, logBuffer)) {
                            adapter.notifyDataSetChanged();
                            DrawChart(logBuffer, chart);
                        }
                    }
                    break;
                case PressureSensor:
                    type = intent.getStringExtra("type");
                    if (intent.getStringExtra("type").startsWith("pres_")) {
                        String log = intent.getStringExtra("log");
                        int wd = Integer.parseInt(type.substring(5));
                        if (Fill5in1SensorLog(wd, scope, log, logBuffer)) {
                            adapter.notifyDataSetChanged();
                            DrawChart(logBuffer, chart);
                        }
                    }
                    break;

                case WaterLevelSensor:
                    type = intent.getStringExtra("type");
                    if (intent.getStringExtra("type").startsWith("tank_")) {
                        String log = intent.getStringExtra("log");
                        int wd = Integer.parseInt(type.substring(5));
                        if (FillWaterLevelLog(wd, widgetId, scope, log, logBuffer)) {
                            adapter.notifyDataSetChanged();
                            DrawChart(logBuffer, chart);
                        }
                    }
                    break;
            }
        }
    }

    class _5in1LogAdapter extends ArrayAdapter<LogTwoValueItem> {

        private final boolean has2ndValue;

        _5in1LogAdapter(Context context, ArrayList<LogTwoValueItem> points, boolean has2ndValue) {
            super(context, 0, points);

            this.has2ndValue = has2ndValue;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_key_value, parent, false);
            }

            LogTwoValueItem point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText(sdf.format(point.date));
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue1)).setText(String.format(Locale.US, "%d", point.Value1));
                if (has2ndValue)
                    ((TextView) convertView.findViewById(R.id.tvListViewItemValue2)).setText(point.Value2);
                else
                    convertView.findViewById(R.id.tvListViewItemValue2).setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);
        menu.findItem(R.id.action_24h).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        item.setChecked(!item.isChecked());

        int wd = -1;
        switch (id) {
            case R.id.action_24h:
                wd = -1;
                break;
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

        logBuffer.clear();
        chart.getData().clearValues();
        chart.clear();

        SetStartAndDates(wd, true);
        return true;
    }

    private void SetStartAndDates(int wd, boolean requestLogs) {

        Calendar calendar = Calendar.getInstance();

        endWeekDay = wd < 0 ? calendar.get(Calendar.DAY_OF_WEEK) - 1 : wd;
        endDate0 = sdf0.format(calendar.getTime());

        if (wd < 0) {
            calendar.add(Calendar.DATE, -1);

            startTime = calendar.getTime();
            startWeekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            startDate0 = sdf0.format(startTime);
        } else {
            calendar.set(Calendar.DAY_OF_WEEK, wd + 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            startTime = calendar.getTime();
            startWeekDay = wd;
            startDate0 = endDate0;
        }

        if (requestLogs) {
            RequestLog(startWeekDay);
            if (wd < 0)
                RequestLog(endWeekDay);
        }
    }

    private static void DrawChart(ArrayList<LogTwoValueItem> logBuffer, LineChart chart) {

        List<Entry> entries = new ArrayList<>();

        Integer lastValue = null;
        for (LogTwoValueItem item : logBuffer) {
            entries.add(new Entry(item.date.getTime(), item.Value1));
            lastValue = item.Value1;
        }

        // add current value (last one)
        if (lastValue != null)
            entries.add(new Entry(new Date().getTime(), lastValue));

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh
    }

    private class TimeValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            try {
                return sdf2.format(new Date((long) value));
            } catch (Exception e) {
                return "?";
            }

        }
    }

    public boolean Fill5in1SensorLog(int wd, WidgetType scope, String log, ArrayList<LogTwoValueItem> logBuffer) {

        String date0;
        if (wd == startWeekDay)
            date0 = startDate0;
        else {
            date0 = endDate0;
        }

        int logEntryLen = 19;

        Date XX;
        int value1 = 0;
        String value2 = "";

        String[] logEntries = log.split(":");
        for (String logEntry : logEntries) {
            if (logEntry.length() == logEntryLen) {
                try {
                    XX = sdfX.parse(date0 + logEntry.substring(0, 6));
                } catch (ParseException ex) {
                    Log.e("Log", "Invalid X", ex);
                    continue;
                }

                if (XX.compareTo(startTime) < 0)
                    continue;

                try {
                    switch (scope) {
                        case WindSensor:
                            value1 = Integer.parseInt(logEntry.substring(11, 15), 16);
                            value2 = String.format(Locale.US, "%d °", Integer.parseInt(logEntry.substring(15, 19), 16));
                            break;
                        case WindDirSensor:
                            value1 = Integer.parseInt(logEntry.substring(15, 19), 16);
                            value2 = String.format(Locale.US, "%d km/h", Integer.parseInt(logEntry.substring(11, 15), 16));
                            break;
                        case RainSensor:
                            value1 = Integer.parseInt(logEntry.substring(11, 15), 16);
                            value2 = String.format(Locale.US, "%d mm", Integer.parseInt(logEntry.substring(15, 19), 16));
                            break;
                        case PressureSensor:
                            value1 = Integer.parseInt(logEntry.substring(11, 15), 16);
                            break;
                    }
                } catch (NumberFormatException ex) {
                    Log.e("Log", "Invalid Y", ex);
                    continue;
                }

                logBuffer.add(new LogTwoValueItem(XX, value1, value2));
            }
        }

        return wd == endWeekDay;
    }


    public boolean FillWaterLevelLog(int wd, int sensorId, WidgetType scope, String log, ArrayList<LogTwoValueItem> logBuffer) {

        boolean result = false;

        String date0;
        if (wd == startWeekDay)
            date0 = startDate0;
        else {
            result = true;
            date0 = endDate0;
        }

        int logEntryLen = 21;

        Date XX;
        int id;
        int value1 = 0;
        String value2 = "";

        String[] logEntries = log.split(":");
        for (String logEntry : logEntries) {
            if (logEntry.length() == logEntryLen) {
                try {
                    XX = sdfX.parse(date0 + logEntry.substring(0, 6));
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

                if (id != sensorId)
                    continue;
                if (XX.compareTo(startTime) < 0)
                    continue;

                try {
                    if (scope == WidgetType.WaterLevelSensor) {
                        value1 = Integer.parseInt(logEntry.substring(11, 15), 16);
                        value2 = String.format(Locale.US, "%d cm %s %s %s",
                                Integer.parseInt(logEntry.substring(7, 11), 16), // distance
                                logEntry.charAt(15) == '0' ? "" : "F", // float switch is full
                                Utils.GetBallValveStateText(Integer.parseInt(logEntry.substring(16, 20), 16)), //  ball valve state
                                logEntry.charAt(20));
                    }
                } catch (NumberFormatException ex) {
                    Log.e("Log", "Invalid Y", ex);
                    continue;
                }

                logBuffer.add(new LogTwoValueItem(XX, value1, value2));
            }
        }

        return result;
    }
}
