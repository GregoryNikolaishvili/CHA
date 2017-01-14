package ge.altasoft.gia.cha;

import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;

import ge.altasoft.gia.cha.classes.LineSeriesArray;
import ge.altasoft.gia.cha.classes.TimeAsXAxisLabelFormatter;
import ge.altasoft.gia.cha.thermostat.BoilerFragment;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class GraphActivity extends ChaActivity {

    private LineSeriesArray pointSeries = new LineSeriesArray(ThermostatControllerData.BOILER_SENSOR_COUNT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++)
            pointSeries.getItem(i).fill(ThermostatControllerData.Instance.boilerSensors(i).getLogBuffer());

        final GraphView graph = (GraphView) findViewById(R.id.graphBig);
        pointSeries.addToGraph(graph);

        graph.getViewport().scrollToEnd();
    }

    @Override
    protected void processThermostatControllerData(int flags) {
        super.processThermostatControllerData(flags);

        if ((flags & Utils.FLAG_HAVE_STATE) != 0) {
            for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++) {
                pointSeries.getItem(i).append(ThermostatControllerData.Instance.boilerSensors(i));
            }
        }
    }
}
