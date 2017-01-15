package ge.altasoft.gia.cha.classes;

import android.graphics.Color;
import android.util.Pair;

import java.util.Date;

import ge.altasoft.gia.cha.Utils;

public class TempSensorData {

    //private boolean enabled;

    protected int id;
    protected int order;

    private double T;
    private char temperatureTrend;

    private double desiredT;
    private double deltaDesiredT;

    private long lastActivitySec;

    private CircularArrayList<Pair<Date, Double>> logBuffer = new CircularArrayList<>(Utils.LOG_BUFFER_SIZE);

    public final static char NO_CHANGE = 'N';
    public final static char GOING_UP = 'U';
    public final static char GOING_DOWN = 'D';

    public TempSensorData(int id) {
        this.id = id;
        //this.enabled = false;
        this.T = 99;
        this.desiredT = 25;
    }

    public int getId() {
        return this.id;
    }

    long getLastActivitySec() {
        return this.lastActivitySec;
    }

    public double getTemperature() {
        return this.T;
    }

    protected void setTemperature(double value) {
        this.lastActivitySec = System.currentTimeMillis() / 1000;

        if (this.T != value) {
            this.T = value;
            logBuffer.add(new Pair<Date, Double>(new Date(), value));
        }
    }

    public char getTemperatureTrend() {
        return this.temperatureTrend;
    }

    protected void setTemperatureTrend(char value) {
        this.temperatureTrend = value;
    }

    public CircularArrayList<Pair<Date, Double>> getLogBuffer() {
        return logBuffer;
    }

//    public String getInfo() {
//        return String.format(Locale.US, "Last sync: %d seconds ago", System.currentTimeMillis() / 1000 - lastActivitySec) + "\n\n" + log.toString();
//    }

    public double getDesiredTemperature() {
        return this.desiredT;
    }

    public void setDesiredTemperature(double value) {
        this.desiredT = value;
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