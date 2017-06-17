package ge.altasoft.gia.cha.classes;

import java.util.Date;

import ge.altasoft.gia.cha.Utils;

public class TempSensorData {

    final protected int id;
    protected int order;

    private float T;
    private char temperatureTrend;

    private float targetT;
    private float deltaTargetT;

    private long lastSyncTime;

    protected TempSensorData(int id) {
        this.id = id;
        this.order = 99;
        this.T = Float.NaN;
        this.targetT = Float.NaN;
        this.temperatureTrend = '=';
    }

    public int getId() {
        return this.id;
    }

    public long getLastSyncTime() {
        return this.lastSyncTime;
    }

    protected void setLastSyncTime(long value) {
        lastSyncTime = value;
    }

    protected void setLastSyncTime() {
        lastSyncTime = new Date().getTime();
    }

    public float getTemperature() {
        return this.T;
    }

    protected void setTemperature(float value) {
        //setLastSyncTime(0);
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
            return Utils.COLOR_TEMP_NORMAL;

        float delta = T - this.targetT;
        if (Math.abs(delta) <= deltaTargetT)
            return Utils.COLOR_TEMP_NORMAL;

        return delta < 0 ? Utils.COLOR_TEMP_LOW : Utils.COLOR_TEMP_HIGH;
    }

//    public int getOrder() {
//        return this.order;
//    }

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

//    public void decodeState(String payload) {
//        char lastChar = payload.charAt(payload.length() - 1);
//        if ((lastChar == '+') || (lastChar == '-') || (lastChar == '=')) {
//            setTemperatureTrend(lastChar);
//            payload = payload.substring(0, payload.length() - 1);
//        }
//        setTemperature(Utils.decodeT(payload));
//    }

}