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

import ge.altasoft.gia.cha.classes.LogTHItem;
import ge.altasoft.gia.cha.classes.WidgetType;

public class LogActivityTH2 extends ChaActivity {

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
    private int sensorId;

    private THLogAdapter adapter = null;
    private ArrayList<LogTHItem> logBuffer;

    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_th2);

        Intent intent = getIntent();
        scope = (WidgetType) intent.getSerializableExtra("widget");
        sensorId = intent.getIntExtra("id", -1);

        logBuffer = new ArrayList<>();
        adapter = new THLogAdapter(this, logBuffer, scope == WidgetType.RoomSensor);

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
            case BoilerSensor:
                publish("cha/hub/getlog", "boiler_".concat(String.valueOf(wd)), false);
                break;
            case RoomSensor:
                publish("cha/hub/getlog", "room_".concat(String.valueOf(wd)), false);
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
                case BoilerSensor:
                    String type = intent.getStringExtra("type");
                    if (type.startsWith("boiler_")) {
                        String log = intent.getStringExtra("log");
                        int wd = Integer.parseInt(type.substring(7));
                        if (FillTHSensorLog(wd, sensorId, scope, log, logBuffer)) {
                            adapter.notifyDataSetChanged();
                            DrawChart(logBuffer, chart);
                        }
                    }
                    break;
                case RoomSensor:
                    type = intent.getStringExtra("type");
                    if (type.startsWith("room_")) {
                        int wd = Integer.parseInt(type.substring(5));
                        String log = intent.getStringExtra("log");
                        if (FillTHSensorLog(wd, sensorId, scope, log, logBuffer)) {
                            adapter.notifyDataSetChanged();
                            DrawChart(logBuffer, chart);
                        }
                    }
                    break;
            }
        }
    }

    class THLogAdapter extends ArrayAdapter<LogTHItem> {

        private final boolean hasHumidity;

        THLogAdapter(Context context, ArrayList<LogTHItem> points, boolean hasHumidity) {
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

            LogTHItem point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText(sdf.format(point.date));
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue1)).setText(String.format(Locale.US, "%.1f°", point.T));

                if (hasHumidity)
                    ((TextView) convertView.findViewById(R.id.tvListViewItemValue2)).setText(String.format(Locale.US, "%.0f %%", point.H));
                else
                    convertView.findViewById(R.id.tvListViewItemValue2).setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    @Override
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

    private static void DrawChart(ArrayList<LogTHItem> logBuffer, LineChart chart) {

        List<Entry> entries = new ArrayList<>();

        for (LogTHItem item : logBuffer) {
            entries.add(new Entry(item.date.getTime(), item.T));
        }

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

    private boolean FillTHSensorLog(int wd, int sensorId, WidgetType scope, String log, ArrayList<LogTHItem> logBuffer) {

        String date0;
        if (wd == startWeekDay)
            date0 = startDate0;
        else {
            date0 = endDate0;
        }

        int logEntryLen = scope == WidgetType.BoilerSensor ? 11 : 18;

        int id;
        Date XX;
        double T, H = 0f;

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
                    if (scope == WidgetType.BoilerSensor)
                        id = Integer.parseInt(logEntry.substring(6, 7), 16);
                    else
                        id = Integer.parseInt(logEntry.substring(6, 10), 16);

                } catch (NumberFormatException ex) {
                    Log.e("Log", "Invalid id", ex);
                    continue;
                }

                if (id != sensorId)
                    continue;

                try {
                    if (scope == WidgetType.BoilerSensor) {
                        T = Utils.decodeT(logEntry.substring(7, 11));
                    } else {
                        T = Utils.decodeT(logEntry.substring(10, 14));
                        H = Integer.parseInt(logEntry.substring(14, 18), 16);
                    }
                } catch (NumberFormatException ex) {
                    Log.e("Log", "Invalid Y", ex);
                    continue;
                }

                logBuffer.add(new LogTHItem(XX, (float) T, (float) H));
            }
        }
        return wd == endWeekDay;
    }

}
