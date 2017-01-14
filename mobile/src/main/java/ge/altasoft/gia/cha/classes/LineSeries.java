package ge.altasoft.gia.cha.classes;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import ge.altasoft.gia.cha.thermostat.TempSensorData;
import ge.altasoft.gia.cha.thermostat.TemperaturePoint;
import ge.altasoft.gia.cha.thermostat.TemperaturePointArray;

public class LineSeries extends LineGraphSeries<DataPoint> {

    public void fill(TemperaturePointArray points) {
        DataPoint[] dataPoints = new DataPoint[points.size()];

        int idx = 0;
        for (TemperaturePoint pt : points)
            dataPoints[idx++] = new DataPoint(pt.first.getTime(), pt.second);

        resetData(dataPoints);
    }

    public void append(TempSensorData data) {
        appendData(new DataPoint(data.getLastActivitySec() * 1000, data.getTemperature()), true, 100);
    }

}


