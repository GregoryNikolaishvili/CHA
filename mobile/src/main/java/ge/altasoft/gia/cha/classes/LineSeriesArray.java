package ge.altasoft.gia.cha.classes;


import android.graphics.Color;

import com.jjoe64.graphview.GraphView;

import java.util.Calendar;
import java.util.Date;

public class LineSeriesArray {

    private TemperatureLineSeries[] series;

    public LineSeriesArray(int count) {
        series = new TemperatureLineSeries[count];

        for (int i = 0; i < count; i++) {
            series[i] = new TemperatureLineSeries();
            series[i].setColor(getDefaultColor(i));
        }
    }

    public TemperatureLineSeries getItem(int index)
    {
        return series[index];
    }

    public void addToGraph(GraphView graph) {

        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();

        calendar.add(Calendar.MINUTE, 1);
        Date d2 = calendar.getTime();

        graph.getGridLabelRenderer().setLabelFormatter(new TimeAsXAxisLabelFormatter("HH:mm"));
        //graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
        //graph.getGridLabelRenderer().setNumVerticalLabels(4); // only 4 because of the space

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d2.getTime());

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0.0);
        graph.getViewport().setMaxY(100.0);
        graph.getGridLabelRenderer().setHumanRounding(false);

        for (TemperatureLineSeries s : series) graph.addSeries(s);
    }

    private int getDefaultColor(int idx) {
        switch (idx) {
            case 0:
                return Color.RED;
            case 1:
                return Color.BLUE;
            case 2:
                return Color.CYAN;
            default:
                return Color.BLACK;
        }
    }
}
