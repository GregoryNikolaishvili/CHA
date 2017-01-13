package ge.altasoft.gia.cha;

import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;

import ge.altasoft.gia.cha.thermostat.BoilerFragment;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class GraphActivity extends ChaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        LineGraphSeries[] graphSeries = BoilerFragment.graphSeriesStatic;
        BoilerFragment.graphSeriesStatic = null;

        final GraphView graph = (GraphView) findViewById(R.id.graphBig);

        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++)
            graph.addSeries(graphSeries[i]);

        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();

        calendar.add(Calendar.MINUTE, 1);
        Date d2 = calendar.getTime();

        graph.getGridLabelRenderer().setLabelFormatter(new TimeAsXAxisLabelFormatter("HH:mm"));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space

        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScrollableY(true); // enables vertical scrolling
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d2.getTime());
        graph.getGridLabelRenderer().setHumanRounding(false);

        graph.getViewport().scrollToEnd();
    }
}
