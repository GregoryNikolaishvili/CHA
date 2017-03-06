package ge.altasoft.gia.cha.thermostat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.SparseArray;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.Log5in1Item;
import ge.altasoft.gia.cha.classes.LogRelayItem;
import ge.altasoft.gia.cha.classes.LogTHItem;
import ge.altasoft.gia.cha.classes.WidgetType;

public final class ThermostatUtils {

    public final static int ACTIVITY_REQUEST_SETTINGS_CODE = 3;

    public static void FillRelayLog(int relayId, WidgetType scope, String log, ArrayList<LogRelayItem> logBuffer) {
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

                logBuffer.add(new LogRelayItem(XX, state));
            }
        }
    }

    public static XYMultipleSeriesRenderer getSensorChartRenderer(Context context, boolean isSmall, int rendererCount, int[] colors) {

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        // We want to avoid black border
        // transparent margins
        renderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        //renderer.setMargins(new int[] { 60, 60, 60, 60 });
        renderer.setMargins(new int[]{30, 70, 10, 0});

        renderer.setClickEnabled(true);
        renderer.setShowGrid(true); // we show the grid
        renderer.setShowLegend(false);
        renderer.setShowLabels(true, true);
        renderer.setShowTickMarks(true);
        renderer.setShowCustomTextGrid(true);

        renderer.setPointSize(5f);
        renderer.setAxesColor(Color.DKGRAY);
        renderer.setLabelsColor(Color.BLACK);

        renderer.setXLabels(0);
        if (isSmall)
            renderer.setYLabels(3);
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);

        renderer.setShowGridY(true);
        //renderer.setYLabelsVerticalPadding(30);
        renderer.setXLabelsPadding(5);
        renderer.setYLabelsPadding(5);

        renderer.setYLabelsAngle(-90);
        renderer.setLabelsTextSize(context.getResources().getDimension(R.dimen.chart_label_size));

        //DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        //float sz = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, metrics);
        //renderer.setLabelsTextSize(sz);

        for (int i = 0; i < rendererCount; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setPointStyle(PointStyle.POINT);
            if (i < colors.length)
                r.setColor(colors[i]);
            //r.setFillPoints(true);
            r.setLineWidth(2);
            // Include low and max value
            r.setDisplayBoundingPoints(true);
            r.setPointStrokeWidth(1);
            renderer.addSeriesRenderer(r);
        }

        return renderer;
    }

    static Date[] DrawBoilerSensorChart(String log, Date startDt, int dateLabelIntervalMinutes, GraphicalView chartView, XYMultipleSeriesRenderer renderer, XYMultipleSeriesDataset xyDataSet) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.US);
        String date0 = sdf.format(new Date());
        sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);

        int logEntryLen = 11;

        for (int i = 0; i < xyDataSet.getSeriesCount(); i++)
            xyDataSet.getSeriesAt(i).clear();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date minXX = calendar.getTime();
        Date maxXX;
        if (startDt != null)
            maxXX = startDt;
        else {
            calendar.add(Calendar.YEAR, -2);
            maxXX = calendar.getTime();
        }

        int id;
        Date XX;
        double T;
        SparseArray<Double> startValues = new SparseArray<>();

        String[] logEntries = log.split(":");
        for (String logEntry : logEntries) {
            if (logEntry.length() == logEntryLen) {
                try {
                    XX = sdf.parse(date0 + logEntry.substring(0, 6));
                } catch (ParseException ex) {
                    Log.e("Chart", "Invalid X", ex);
                    continue;
                }

                try {
                    id = Integer.parseInt(logEntry.substring(6, 7), 16);
                } catch (NumberFormatException ex) {
                    Log.e("Chart", "Invalid id", ex);
                    continue;
                }

                try {
                    T = Utils.decodeT(logEntry.substring(7, 11));
                } catch (NumberFormatException ex) {
                    Log.e("Chart", "Invalid Y", ex);
                    continue;
                }

                if ((startDt != null) && XX.before(startDt)) {
                    startValues.put(id, T);
                    continue;
                }

                XYSeries series = xyDataSet.getSeriesAt(id);
                if ((startDt != null) && (series.getItemCount() == 0) && (startValues.get(id) != null))
                    series.add(startDt.getTime(), startValues.get(id));
                series.add(XX.getTime(), T);

                if (XX.before(minXX))
                    minXX = XX;
                if (XX.after(maxXX))
                    maxXX = XX;
            }
        }

        // add current values (last one)
        for (int i = 0; i < xyDataSet.getSeriesCount(); i++) {
            XYSeries series = xyDataSet.getSeriesAt(i);
            if (series.getItemCount() > 0)
                series.add(new Date().getTime(), series.getY(series.getItemCount() - 1));
        }

        sdf = new SimpleDateFormat("HH:mm", Locale.US);

        calendar.setTime(minXX);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        minXX = calendar.getTime();

        calendar.setTime(maxXX);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, dateLabelIntervalMinutes);
        maxXX = calendar.getTime();

        calendar.setTime(minXX);
        while (calendar.getTime().getTime() <= maxXX.getTime()) {
            renderer.addXTextLabel(calendar.getTime().getTime(), sdf.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, dateLabelIntervalMinutes);
        }

        chartView.repaint();

        return new Date[]{minXX, maxXX};
    }

    public static void FillTHSensorLog(int sensorId, WidgetType scope, String log, ArrayList<LogTHItem> logBuffer) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.US);
        String date0 = sdf.format(new Date());
        sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);

        int logEntryLen = scope == WidgetType.BoilerSensor ? 11 : 18;

        logBuffer.clear();

        int id;
        Date XX;
        double T, H = 0f;


        String[] logEntries = log.split(":");
        for (String logEntry : logEntries) {
            if (logEntry.length() == logEntryLen) {
                try {
                    XX = sdf.parse(date0 + logEntry.substring(0, 6));
                } catch (ParseException ex) {
                    Log.e("Log", "Invalid X", ex);
                    continue;
                }

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
    }

    public static Date[] DrawTHSensorChart(ArrayList<LogTHItem> logBuffer, GraphicalView chartView, XYMultipleSeriesRenderer renderer, XYMultipleSeriesDataset xyDataSet) {

        for (int i = 0; i < xyDataSet.getSeriesCount(); i++)
            xyDataSet.getSeriesAt(i).clear();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date minXX = calendar.getTime();
        calendar.add(Calendar.YEAR, -2);
        Date maxXX = calendar.getTime();

        Date XX;
        XYSeries series = xyDataSet.getSeriesAt(0);

        for (LogTHItem item : logBuffer) {
            XX = item.date;

            series.add(XX.getTime(), item.T);

            if (XX.before(minXX))
                minXX = XX;
            if (XX.after(maxXX))
                maxXX = XX;
        }

        // add current value (last one)
        if (series.getItemCount() > 0)
            series.add(new Date().getTime(), series.getY(series.getItemCount() - 1));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);

        calendar.setTime(minXX);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        minXX = calendar.getTime();

        calendar.setTime(maxXX);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, 120);
        maxXX = calendar.getTime();

        calendar.setTime(minXX);
        while (calendar.getTime().getTime() <= maxXX.getTime()) {
            renderer.addXTextLabel(calendar.getTime().getTime(), sdf.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, 120);
        }

        chartView.repaint();

        return new Date[]{minXX, maxXX};
    }

    public static void Fill5in1SensorLog(WidgetType scope, String log, ArrayList<Log5in1Item> logBuffer) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.US);
        String date0 = sdf.format(new Date());
        sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);

        int logEntryLen = 27;

        logBuffer.clear();

        Date XX;
        int value1 = 0, value2 = 0;


        String[] logEntries = log.split(":");
        for (String logEntry : logEntries) {
            if (logEntry.length() == logEntryLen) {
                try {
                    XX = sdf.parse(date0 + logEntry.substring(0, 6));
                } catch (ParseException ex) {
                    Log.e("Log", "Invalid X", ex);
                    continue;
                }

                try {
                    switch (scope) {
                        case WindSensor:
                            value1 = Integer.parseInt(logEntry.substring(11, 15), 16);
                            value2 = Integer.parseInt(logEntry.substring(15, 19), 16);
                            break;
                        case PressureSensor:
                            value1 = Integer.parseInt(logEntry.substring(23, 27), 16);
                            break;
                        case RainSensor:
                            value1 = Integer.parseInt(logEntry.substring(19, 24), 16);
                            break;
                    }
                } catch (NumberFormatException ex) {
                    Log.e("Log", "Invalid Y", ex);
                    continue;
                }

                logBuffer.add(new Log5in1Item(XX, value1, value2));
            }
        }
    }

    public static Date[] Draw5in1Chart(ArrayList<Log5in1Item> logBuffer, GraphicalView chartView, XYMultipleSeriesRenderer renderer, XYMultipleSeriesDataset xyDataSet) {

        for (int i = 0; i < xyDataSet.getSeriesCount(); i++)
            xyDataSet.getSeriesAt(i).clear();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date minXX = calendar.getTime();
        calendar.add(Calendar.YEAR, -2);
        Date maxXX = calendar.getTime();

        Date XX;
        XYSeries series = xyDataSet.getSeriesAt(0);

        for (Log5in1Item item : logBuffer) {
            XX = item.date;

            series.add(XX.getTime(), item.Value1);

            if (XX.before(minXX))
                minXX = XX;
            if (XX.after(maxXX))
                maxXX = XX;
        }

        // add current value (last one)
        if (series.getItemCount() > 0)
            series.add(new Date().getTime(), series.getY(series.getItemCount() - 1));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);

        calendar.setTime(minXX);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        minXX = calendar.getTime();

        calendar.setTime(maxXX);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.MINUTE, 120);
        maxXX = calendar.getTime();

        calendar.setTime(minXX);
        while (calendar.getTime().getTime() <= maxXX.getTime()) {
            renderer.addXTextLabel(calendar.getTime().getTime(), sdf.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, 120);
        }

        chartView.repaint();

        return new Date[]{minXX, maxXX};
    }
}