package ge.altasoft.gia.cha.classes;

import android.graphics.Color;
import android.util.Pair;

import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.Utils;

public class TempSensorData {

    //private boolean enabled;

    final protected int id;
    protected int order;

    private float T;
    private char temperatureTrend;

    private float targetT;
    private float deltaTargetT;

    private long lastActivitySec;

    final private CircularArrayList<Pair<Date, Float>> logBuffer = new CircularArrayList<>(Utils.LOG_BUFFER_SIZE);

    public final static char NO_CHANGE = 'N';
    public final static char GOING_UP = 'U';
    public final static char GOING_DOWN = 'D';

    protected TempSensorData(int id) {
        this.id = id;
        //this.enabled = false;
        this.T = 99;
        this.targetT = Utils.DEFAULT_TARGET_TEMPERATURE;
    }

    public int getId() {
        return this.id;
    }

    long getLastActivitySec() {
        return this.lastActivitySec;
    }

    public float getTemperature() {
        return this.T;
    }

    private void setTemperature(float value) {
        this.lastActivitySec = System.currentTimeMillis() / 1000;

        if (this.T != value) {
            this.T = value;
            logBuffer.add(new Pair<>(new Date(), value));
        }
    }

    public char getTemperatureTrend() {
        return this.temperatureTrend;
    }

    private void setTemperatureTrend(char value) {
        this.temperatureTrend = value;
    }

    public CircularArrayList<Pair<Date, Float>> getLogBuffer() {
        return logBuffer;
    }

//    public String getInfo() {
//        return String.format(Locale.US, "Last sync: %d seconds ago", System.currentTimeMillis() / 1000 - lastActivitySec) + "\n\n" + log.toString();
//    }

    public float getTargetTemperature() {
        return this.targetT;
    }

    public void setTargetTemperature(float value) {
        this.targetT = value;
    }

    protected void setDeltaTargetT(float value) {
        deltaTargetT = value;
    }

    public int getTemperatureColor() {
        if (this.targetT == 0)
            return Color.BLACK;

        float delta = T - this.targetT;
        if (Math.abs(delta) <= deltaTargetT)
            return Color.BLACK;

        return delta < 0 ? Color.BLUE : Color.RED;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    protected void encodeSettings(StringBuilder sb) {
        sb.append(String.format(Locale.US, "%04X%04X", ((Float) (targetT * 10)).intValue(), ((Float) (deltaTargetT * 10)).intValue()));
    }

    protected int decodeSettings(String response, int idx) {
        setTargetTemperature(Integer.parseInt(response.substring(idx, idx + 4), 16) / 10.0f);
        setDeltaTargetT(Integer.parseInt(response.substring(idx + 4, idx + 8), 16) / 10.0f);
        return idx + 8;
    }

    public void encodeState(StringBuilder sb) {
        if (Utils.DEBUG_THERMOSTAT) {
            int trend = Utils.random(0, 2);
            char c = NO_CHANGE;
            if (trend == 1)
                c = GOING_UP;
            else if (trend == 2)
                c = GOING_DOWN;

            sb.append(String.format(Locale.US, "%04X%c", Utils.random(10, 100) * 10, c));
        } else
            sb.append(String.format(Locale.US, "%04X%c", ((Float) (T * 10)).intValue(), getTemperatureTrend()));
    }

    public int decodeState(String value, int idx) {
        setTemperature(Integer.parseInt(value.substring(idx, idx + 4), 16) / 10.0f);
        setTemperatureTrend(value.charAt(idx + 4));

        return idx + 5;
    }
}