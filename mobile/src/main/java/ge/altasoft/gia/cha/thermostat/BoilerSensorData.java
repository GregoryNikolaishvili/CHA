package ge.altasoft.gia.cha.thermostat;

import android.graphics.Color;

import java.util.Locale;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.BoilerSensorView;

public final class BoilerSensorData extends TempSensorData{

    private BoilerSensorView boilerSensorView;

    public BoilerSensorData(int id) {
        super(id);

        setDeltaDesiredT(5);
    }


    public void setBoilerSensorView(BoilerSensorView boilerSensorView) {
        this.boilerSensorView = boilerSensorView;
    }

    public void encodeState(StringBuilder sb) {
        if (Utils.DEBUG_THEROSTAT) {
            int trend = ((Double) (Math.random() * 3)).intValue();
            char c = NO_CHANGE;
            if (trend == 1)
                c = GOING_UP;
            else if (trend == 2)
                c = GOING_DOWN;

            sb.append(String.format(Locale.US, "%04X%c", ((Double) (Math.random() * 100)).intValue(), c));
        } else
            sb.append(String.format(Locale.US, "%04X%c", ((Double) (getTemperature() * 10)).intValue(), temperatureTrend));
    }

    public int decodeState(String value, int idx) {
        setTemperature(Integer.parseInt(value.substring(idx, idx + 4), 16) / 10.0);
        temperatureTrend = value.charAt(idx + 4);
        this.lastActivitySec = System.currentTimeMillis() / 1000;

        if (boilerSensorView != null)
            boilerSensorView.setSensorData(this);

        return idx + 5;
    }
}