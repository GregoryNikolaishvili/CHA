package ge.altasoft.gia.cha.thermostat;

import android.graphics.Color;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.RoomSensorView;

public class TempSensorData {

    protected boolean enabled;

    protected int id;
    protected int order;

    private double T;
    protected char temperatureTrend;

    protected double desiredT;
    private double deltaDesiredT;

    protected long lastActivitySec;

    private StringBuilder log;

    public final static char NO_CHANGE = 'N';
    public final static char GOING_UP = 'U';
    public final static char GOING_DOWN = 'D';

    public TempSensorData(int id) {
        this.id = id;
        this.enabled = false;
        this.T = 99;
        this.desiredT = 25;

        log = new StringBuilder();
    }

    public int getId() {
        return this.id;
    }

    public double getTemperature() {
        return this.T;
    }

    public int getTemperatureTrend() {
        return this.temperatureTrend;
    }

    public String getInfo()
    {
      return String.format("Last sync: %d seconds ago", System.currentTimeMillis() / 1000 -  lastActivitySec) + "\n\n" + log.toString();
    }
    public double getDesiredTemperature() {
        return this.desiredT;
    }

    protected void setTemperature(double value) {
        T = value;

        String now = DateFormat.getDateTimeInstance().format(new Date());
        log.append(now);
        log.append(": ");
        log.append(value);
        log.append("°\n");
    }

    protected void setDeltaDesiredT(double value) {
        deltaDesiredT = value;
    }

    public int getTemperatureColor() {
        if (this.desiredT == 0)
            return Color.BLACK;

        double delta = T - this.desiredT;
        if (Math.abs(delta) <= deltaDesiredT)
            return Color.BLACK;

        return delta < 0 ? Color.BLUE : Color.RED;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}