package ge.altasoft.gia.cha.thermostat;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

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

import ge.altasoft.gia.cha.Utils;

public final class ThermostatUtils {

    public final static int ACTIVITY_REQUEST_SETTINGS_CODE = 3; // $ + 12 switches + autoatic_mode + datetime  + sunrise/sunset 123456789012TYYMMDDHHmmssxxxxxxxx. 34 chars

    public static Date[] DrawBoilerSensorChart(String log, GraphicalView chartView, XYMultipleSeriesRenderer renderer, XYMultipleSeriesDataset xyDataSet, Date startDt, int dateLabelIntervalMinutes) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.US);
        String date0 = sdf.format(new Date());
        sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);

        for (int i = 0; i < xyDataSet.getSeriesCount(); i++)
            xyDataSet.getSeriesAt(i).clear();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 1);
        Date minXX = calendar.getTime();
        Date maxXX = startDt;

        int id;
        Date XX;
        double Y;

        String[] logEntries = log.split(":");
        for (String logEntry : logEntries) {
            if (logEntry.length() == 11) {
                try {
                    XX = sdf.parse(date0 + logEntry.substring(0, 6));
                } catch (ParseException ex) {
                    Log.e("Graph", "Invalid X", ex);
                    continue;
                }

                if (XX.before(startDt))
                    continue;

                try {
                    id = Integer.parseInt(logEntry.substring(6, 7), 16) - 1;
                } catch (NumberFormatException ex) {
                    Log.e("Graph", "Invalid id", ex);
                    continue;
                }
                try {
                    Y = Utils.decodeT(logEntry.substring(7, 11));
                } catch (NumberFormatException ex) {
                    Log.e("Graph", "Invalid Y", ex);
                    continue;
                }

                xyDataSet.getSeriesAt(id).add(XX.getTime(), Y);

                if (XX.before(minXX))
                    minXX = XX;
                if (XX.after(maxXX))
                    maxXX = XX;
            }
        }

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

    public static XYMultipleSeriesRenderer getBoilerSensorChartRenderer() {

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        // We want to avoid black border
        // transparent margins
        renderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
        //renderer.setMargins(new int[] { 60, 60, 60, 60 });

        renderer.setClickEnabled(true);
        renderer.setShowGrid(true); // we show the grid
        renderer.setShowLegend(false);
        renderer.setShowLabels(true, true);
        renderer.setShowTickMarks(true);
        renderer.setShowCustomTextGrid(true);

        renderer.setPointSize(5f);
        renderer.setAxesColor(Color.DKGRAY);
        renderer.setLabelsColor(Color.BLACK);

        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setXLabels(0);

        renderer.setXLabelsPadding(5);
        renderer.setYLabelsPadding(5);

        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setPointStyle(PointStyle.POINT);
        r.setColor(Color.RED);
        //r.setFillPoints(true);
        //r.setLineWidth(2);
        // Include low and max value
        r.setDisplayBoundingPoints(true);
        r.setPointStrokeWidth(1);
        renderer.addSeriesRenderer(r);

        r = new XYSeriesRenderer();
        r.setPointStyle(PointStyle.POINT);
        r.setColor(Color.GREEN);
        //r.setFillPoints(true);
        //r.setLineWidth(2);
        // Include low and max value
        r.setDisplayBoundingPoints(true);
        r.setPointStrokeWidth(1);
        renderer.addSeriesRenderer(r);

        r = new XYSeriesRenderer();
        r.setPointStyle(PointStyle.POINT);
        //r.setFillPoints(true);
        //r.setLineWidth(2);
        // Include low and max value
        r.setDisplayBoundingPoints(true);
        r.setPointStrokeWidth(1);
        renderer.addSeriesRenderer(r);

        return renderer;
    }

    public static void FillSensorLog(String log, ArrayList<LogItem> logBuffer, int sensorId, String scope) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.US);
        String date0 = sdf.format(new Date());
        sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);

        boolean isRoomSensorLog = !scope.equals("BoilerSensor");
        int logEntryLen = isRoomSensorLog ? 18 : 11;

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
                    if (isRoomSensorLog)
                        id = Integer.parseInt(logEntry.substring(6, 10), 16);
                    else
                        id = Integer.parseInt(logEntry.substring(6, 7), 16) - 1;
                } catch (NumberFormatException ex) {
                    Log.e("Log", "Invalid id", ex);
                    continue;
                }

                if (id != sensorId)
                    continue;

                try {
                    if (isRoomSensorLog) {
                        T = Utils.decodeT(logEntry.substring(10, 14));
                        H = Integer.parseInt(logEntry.substring(14, 18), 16);
                    } else {
                        T = Utils.decodeT(logEntry.substring(7, 11));
                    }
                } catch (NumberFormatException ex) {
                    Log.e("Log", "Invalid Y", ex);
                    continue;
                }

                logBuffer.add(new LogItem(XX, (float) T, (float) H));
            }
        }
    }

}