package ge.altasoft.gia.cha.classes;


import android.graphics.Color;

import com.jjoe64.graphview.GraphView;

public class LineSeriesArray {

    final private TemperatureLineSeries[] series;

    public LineSeriesArray(int count) {
        series = new TemperatureLineSeries[count];

        for (int i = 0; i < count; i++) {
            series[i] = new TemperatureLineSeries();
            series[i].setColor(getDefaultColor(i));
        }
    }

    public TemperatureLineSeries getItem(int index) {
        return series[index];
    }

    public void addToGraph(GraphView graph) {

        graph.getGridLabelRenderer().setLabelFormatter(new TimeAsXAxisLabelFormatter("HH:mm"));
        graph.getGridLabelRenderer().setHumanRounding(true);

        //graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space
        //graph.getGridLabelRenderer().setNumVerticalLabels(4); // only 4 because of the space

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
