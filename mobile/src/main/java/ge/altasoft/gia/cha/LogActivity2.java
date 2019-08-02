package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ge.altasoft.gia.cha.classes.LogTwoValueItem;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class LogActivity2 extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
    final private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.US);

    private WidgetType scope;
    private int widgetId;

    private _5in1LogAdapter adapter = null;
    private ArrayList<LogTwoValueItem> logBuffer;

    LineChart chart;

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
    }

    private void RequestLog(int wd)
    {
        switch (scope) {
            case WindSensor:
            case WindDirSensor:
                publish("cha/hub/getlog", "wind_".concat(String.valueOf(wd)), false);
            case RainSensor:
                publish("cha/hub/getlog", "rain_".concat(String.valueOf(wd)), false);
            case PressureSensor:
                publish("cha/hub/getlog", "pressure_".concat(String.valueOf(wd)), false);
               break;
            case WaterLevelSensor:
                publish("cha/hub/getlog", "tank_".concat(String.valueOf(wd)), false);
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

        if (dataType == MqttClientLocal.MQTTReceivedDataType.Log) {
            switch (scope) {
                case WindSensor:
                case WindDirSensor:
                    if (intent.getStringExtra("type").startsWith("wind_")) {
                        String log = intent.getStringExtra("log");
                        ThermostatUtils.Fill5in1SensorLog(scope, log, logBuffer);
                        adapter.notifyDataSetChanged();
                        DrawChart(logBuffer, chart);
                    }
                    break;
                case RainSensor:
                    if (intent.getStringExtra("type").startsWith("rain_")) {
                        String log = intent.getStringExtra("log");
                        ThermostatUtils.Fill5in1SensorLog(scope, log, logBuffer);
                        adapter.notifyDataSetChanged();
                        DrawChart(logBuffer, chart);
                    }
                    break;
                case PressureSensor:
                    if (intent.getStringExtra("type").startsWith("pressure_")) {
                        String log = intent.getStringExtra("log");
                        ThermostatUtils.Fill5in1SensorLog(scope, log, logBuffer);
                        adapter.notifyDataSetChanged();
                        DrawChart(logBuffer, chart);
                    }
                    break;

                case WaterLevelSensor:
                    if (intent.getStringExtra("type").startsWith("tank")) {
                        String log = intent.getStringExtra("log");
                        ThermostatUtils.FillWaterLevelLog(widgetId, scope, log, logBuffer);
                        adapter.notifyDataSetChanged();
                        DrawChart(logBuffer, chart);
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
        dataSet.setDrawValues(true);

        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.1f);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // refresh
    }

    private class TimeValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            try{
                return sdf2.format(new Date((long)value));
            }
            catch (Exception e)
            {
                return  "?";
            }

        }
    }
}
