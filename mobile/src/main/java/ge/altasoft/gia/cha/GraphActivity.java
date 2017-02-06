package ge.altasoft.gia.cha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.classes.LineSeriesArray;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class GraphActivity extends ChaActivity {

    final private LineSeriesArray pointSeries = new LineSeriesArray(ThermostatControllerData.BOILER_SENSOR_COUNT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

//        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++)
//            pointSeries.getItem(i).fill(ThermostatControllerData.Instance.boilerSensors(i).getLogBuffer());

        final GraphView graph = (GraphView) findViewById(R.id.graphBig);
        pointSeries.addToGraph(graph);

        //graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
        //graph.getGridLabelRenderer().setNumVerticalLabels(8); // only 4 because of the space

        //graph.getViewport().setXAxisBoundsManual(false);
        //graph.getViewport().setYAxisBoundsManual(false);
        //graph.getViewport().setMinY(0.0);
        //graph.getViewport().setMaxY(100.0);
        //graph.getGridLabelRenderer().setHumanRounding(true);

        //graph.getViewport().scrollToEnd();
    }


    @Override
    void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        switch (dataType) {
            case ThermostatBoilerSensorState:
                int id = intent.getIntExtra("id", 0);
                id--;

                pointSeries.getItem(id).append(ThermostatControllerData.Instance.boilerSensors(id));
                break;

            case ThermostatLog:
                if (intent.getStringExtra("type").startsWith("boiler"))
                    rebuildGraph(intent.getStringExtra("log"));
                break;
        }
    }

    @Override
    protected void ServiceConnected() {
        super.ServiceConnected();

        publish("cha/hub/getlog", "boiler_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
    }

    public void rebuildGraph(String log) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.US);
        String date0 = sdf.format(new Date());
        sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);

        int id;
        long X;
        double Y;
        //long minX = Long.MAX_VALUE, maxX = -Long.MAX_VALUE;
        //double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++) {
            DataPoint[] dataPoints = new DataPoint[0];
            pointSeries.getItem(i).resetData(dataPoints);
        }

        String[] logEntries = log.split(":");
        for (String logEntry : logEntries) {
            if (logEntry.length() == 11) {
                try {
                    X = sdf.parse(date0 + logEntry.substring(0, 6)).getTime();
                } catch (ParseException ex) {
                    Log.e("Graph", "Invalid X", ex);
                    return;
                }

                try {
                    id = Integer.parseInt(logEntry.substring(6, 7), 16) - 1;
                } catch (NumberFormatException ex) {
                    Log.e("Graph", "Invalid id", ex);
                    return;
                }
                try {
                    Y = Utils.decodeT(logEntry.substring(7, 11));
                } catch (NumberFormatException ex) {
                    Log.e("Graph", "Invalid Y", ex);
                    return;
                }

                pointSeries.getItem(id).appendData(new DataPoint(X, Y), false, Utils.LOG_BUFFER_SIZE);
//
//                if (X < minX)
//                    minX = X;
//                if (X > maxX)
//                    maxX = X;
//
//                if (Y < minY)
//                    minY = Y;
//                if (Y > maxY)
//                    maxY = Y;
            }
        }
//
//        final GraphView graph = (GraphView) findViewById(R.id.graphBig);
//
//        graph.getViewport().setMinX(minX);
//        graph.getViewport().setMaxX(maxX);
//
//        graph.getViewport().setMinY(minY);
//        graph.getViewport().setMaxY(maxY);
    }

}
