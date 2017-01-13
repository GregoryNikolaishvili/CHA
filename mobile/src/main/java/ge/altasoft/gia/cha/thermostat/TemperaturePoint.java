package ge.altasoft.gia.cha.thermostat;

import android.util.Pair;

import java.util.Date;

public class TemperaturePoint extends Pair<Date, Double> {

    public TemperaturePoint(Date first, Double second) {
        super(first, second);
    }
}
