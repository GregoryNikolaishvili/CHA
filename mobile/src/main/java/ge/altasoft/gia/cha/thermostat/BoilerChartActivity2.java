package ge.altasoft.gia.cha.thermostat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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

import ge.altasoft.gia.cha.ChaActivity;
import ge.altasoft.gia.cha.MqttClientLocal;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.LogOneTValueItem;

public class BoilerChartActivity2 extends ChaActivity {

    final private SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.US);
    final private SimpleDateFormat sdfX = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
    final private SimpleDateFormat sdf0 = new SimpleDateFormat("yyMMdd", Locale.US);

    private Date startTime;

    private int endWeekDay;
    private int startWeekDay;
    private String startDate0;
    private String endDate0;

    private LineChart chart;

    private final ArrayList<LogOneTValueItem>[] chartValues = new ArrayList[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph2);

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

        // dates
        Calendar calendar = Calendar.getInstance();

        //Date endTime = calendar.getTime();
        endWeekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        endDate0 = sdf0.format(calendar.getTime());

        calendar.add(Calendar.DATE, -1);
        startTime = calendar.getTime();
        startWeekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        startDate0 = sdf0.format(calendar.getTime());

        //
        for (int i = 0; i < 3; i++) {
            chartValues[i] = new ArrayList<>();
        }
    }

    @Override
    public void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        if (dataType == MqttClientLocal.MQTTReceivedDataType.Log) {
            String type = intent.getStringExtra("type");
            if (type.startsWith("boiler_")) {
                String log = intent.getStringExtra("log");
                int wd = Integer.parseInt(type.substring(7));
                if (FillBoilerSensorChart(wd, log, chartValues)) {
                    DrawChart(chartValues, chart);
                }
            }
        }
    }

    private void RequestLog(int wd) {
        publish("cha/hub/getlog", "boiler_".concat(String.valueOf(wd)), false);
    }

    @Override
    protected void ServiceConnected() {
        super.ServiceConnected();

        for (ArrayList<LogOneTValueItem> entries : chartValues)
            entries.clear();

        RequestLog(startWeekDay);
        RequestLog(endWeekDay);
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

        for (ArrayList<LogOneTValueItem> entries : chartValues)
            entries.clear();
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

     private boolean FillBoilerSensorChart(int wd, String log, ArrayList<LogOneTValueItem>[] values) {

        String date0;
        if (wd == startWeekDay)
            date0 = startDate0;
        else {
            date0 = endDate0;
        }

        int logEntryLen = 11;

        int id;
        Date XX;
        float T;

        String[] logEntries = log.split(":");
        for (String logEntry : logEntries) {
            if (logEntry.length() == logEntryLen) {
                try {
                    XX = sdfX.parse(date0 + logEntry.substring(0, 6));
                } catch (ParseException ex) {
                    Log.e("Chart", "Invalid X", ex);
                    continue;
                }

                if (XX.compareTo(startTime) < 0)
                    continue;

                try {
                    id = Integer.parseInt(logEntry.substring(6, 7), 16);
                } catch (NumberFormatException ex) {
                    Log.e("Chart", "Invalid id", ex);
                    continue;
                }

                if (id < 0 || id > 2) // skip room temperature
                    continue;

                try {
                    T = Utils.decodeT(logEntry.substring(7, 11));
                } catch (NumberFormatException ex) {
                    Log.e("Chart", "Invalid Y", ex);
                    continue;
                }

                if (T == Utils.F_UNDEFINED)
                    continue;

                values[id].add(new LogOneTValueItem(XX, T));
            }
        }

        return wd == endWeekDay;
    }

    private static void DrawChart(ArrayList<LogOneTValueItem>[] values, LineChart chart) {

        LineData chartData = new LineData();

        for (int i = 0; i < values.length; i++) {
            ArrayList<LogOneTValueItem> temps = values[i];

            List<Entry> entries = new ArrayList<>();

            Float lastValue = null;
            for (LogOneTValueItem item : temps) {
                entries.add(new Entry(item.date.getTime(), item.T));
                lastValue = item.T;
            }

            // add current value (last one)
            if (lastValue != null)
                entries.add(new Entry(new Date().getTime(), lastValue));

            LineDataSet dataSet = new LineDataSet(entries, "");
            dataSet.setDrawCircles(false);
            dataSet.setColor(GetLineColor(i));
            chartData.addDataSet(dataSet);
        }

        chart.setData(chartData);
        chart.invalidate(); // refresh
    }

    private static int GetLineColor(int id) {
        switch (id) {
            case 0:
                return Color.YELLOW;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.RED;
            default:
                return Color.WHITE;
        }
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
}
