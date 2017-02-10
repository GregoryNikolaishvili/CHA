package ge.altasoft.gia.cha.classes;

import android.graphics.Color;
import android.util.Pair;

import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.Utils;

public class TempSensorData {

    final protected int id;
    protected int order;

    private float T;
    private char temperatureTrend;

    private float targetT;
    private float deltaTargetT;

    private long lastReadingTime;

    protected TempSensorData(int id) {
        this.id = id;
        //this.enabled = false;
        this.T = Float.NaN;
        this.targetT = Float.NaN;
    }

    public int getId() {
        return this.id;
    }

    public long getLastReadingTime() {
        return this.lastReadingTime;
    }

    public float getTemperature() {
        return this.T;
    }

    public void setTemperature(float value) {
        this.lastReadingTime = new Date().getTime();
        if (value == Utils.F_UNDEFINED)
            this.T = Float.NaN;
        else
            this.T = value;
    }

    public char getTemperatureTrend() {
        return this.temperatureTrend;
    }

    protected void setTemperatureTrend(char value) {
        this.temperatureTrend = value;
    }

    public float getTargetTemperature() {
        return this.targetT;
    }

    public void setTargetTemperature(float value) {
        if (value == Utils.F_UNDEFINED)
            this.targetT = Float.NaN;
        else
            this.targetT = value;
    }

    protected void setDeltaTargetT(float value) {
        deltaTargetT = value;
    }

    public int getTemperatureColor() {
        if (Float.isNaN(this.targetT))
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
        Utils.encodeT(sb, targetT);
    }

    protected int decodeSettings(String response, int idx) {
        setTargetTemperature(Utils.decodeT(response.substring(idx, idx + 4)));
        return idx + 4;
    }

    public void decodeState(String payload) {
        char lastChar = payload.charAt(payload.length() - 1);
        if ((lastChar == '+') || (lastChar == '-') || (lastChar == '=')) {
            setTemperatureTrend(lastChar);
            payload = payload.substring(0, payload.length() - 1);
        }
        setTemperature(Utils.decodeT(payload));
    }
}