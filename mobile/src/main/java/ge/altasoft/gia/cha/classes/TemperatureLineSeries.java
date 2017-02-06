package ge.altasoft.gia.cha.classes;

import android.util.Pair;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;

import ge.altasoft.gia.cha.Utils;

public class TemperatureLineSeries extends LineGraphSeries<DataPoint> {

    public void fill(CircularArrayList<Pair<Date, Float>> points) {
        DataPoint[] dataPoints = new DataPoint[points.size()];

        int idx = 0;
        for (Pair<Date, Float> pt : points)
            dataPoints[idx++] = new DataPoint(pt.first.getTime(), pt.second);

        resetData(dataPoints);
    }

    public void append(TempSensorData data) {
        double x = data.getLastActivitySec() * 1000;
        String s = Utils.millisToTimeString("HH:mm:ss", x);
        String s2 = Utils.millisToTimeString("HH:mm:ss", getHighestValueX());
        if (x > getHighestValueX()) {
            appendData(new DataPoint(x, data.getTemperature()), false, Utils.LOG_BUFFER_SIZE);
            s2 = Utils.millisToTimeString("HH:mm:ss", getHighestValueX());
        }
    }
}


