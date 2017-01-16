package ge.altasoft.gia.cha.classes;

import android.util.Pair;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;

import ge.altasoft.gia.cha.Utils;

public class TemperatureLineSeries extends LineGraphSeries<DataPoint> {

    public void fill(CircularArrayList<Pair<Date, Double>> points) {
        DataPoint[] dataPoints = new DataPoint[points.size()];

        int idx = 0;
        for (Pair<Date, Double> pt : points)
            dataPoints[idx++] = new DataPoint(pt.first.getTime(), pt.second);

        resetData(dataPoints);
    }

    public void append(TempSensorData data) {
        appendData(new DataPoint(data.getLastActivitySec() * 1000, data.getTemperature()), true, Utils.LOG_BUFFER_SIZE);
    }

}

