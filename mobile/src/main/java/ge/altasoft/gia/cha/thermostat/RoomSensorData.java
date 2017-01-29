package ge.altasoft.gia.cha.thermostat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.MqttClient;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.CircularArrayList;
import ge.altasoft.gia.cha.classes.TempSensorData;

import static ge.altasoft.gia.cha.MqttClient.MQTT_DATA_TYPE;

public final class RoomSensorData extends TempSensorData implements Comparable<RoomSensorData> {

    private float H;
    private int signalLevel;
    private String batteryLevel;
    private String name;
    private boolean canBeControlled;
    final private CircularArrayList<Pair<Date, Float>> logBufferH = new CircularArrayList<>(Utils.LOG_BUFFER_SIZE);

    RoomSensorData(int id) {
        super(id);

        H = Float.NaN;
        canBeControlled = false;
        setDeltaTargetT(1.0f);
        name = String.valueOf(id);
        signalLevel = 0;
        batteryLevel = "unknown";
    }

    public CircularArrayList<Pair<Date, Float>> getLogBufferH() {
        return logBufferH;
    }

    public String getName() {
        return this.name;
    }

    public float getHumidity() {
        return this.H;
    }

    public void setHumidity(float value) {
        if (this.H != value) {
            this.H = value;
            logBufferH.add(new Pair<>(new Date(), value));
        }
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public boolean canBeControlled() {
        return this.canBeControlled;
    }

    public void setCanBeControlled(boolean canBeControlled) {
        this.canBeControlled = canBeControlled;
    }

    void encodeOrderAndName(StringBuilder sb2) {
        sb2.append(String.format(Locale.US, "%08X", this.id));
        sb2.append(String.format(Locale.US, "%01X", this.order));
        sb2.append(Utils.encodeArduinoString(name));
        sb2.append(';');
    }

    void decodeOrderAndName(String s) {
        order = Character.digit(s.charAt(2), 16);
        name = Utils.decodeArduinoString(s.substring(3));
    }

    public void encodeSettings(StringBuilder sb) {
        super.encodeSettings(sb);
        sb.append(canBeControlled ? 'C' : 'N');
    }

    public int decodeSettings(String response, int idx) {
        idx = super.decodeSettings(response, idx);
        canBeControlled = response.charAt(idx) != 'N';
        return idx + 1;
    }

//    public void encodeState(StringBuilder sb) {
//        super.encodeState(sb);
//        sb.append(String.format(Locale.US, "%04X", ((Float) (H * 10)).intValue()));
//    }
//
//    public int decodeState(String value, int idx) {
//        idx = super.decodeState(value, idx);
//        setHumidity(Integer.parseInt(value.substring(idx, idx + 4), 16) / 10.0f);
//        return idx + 4;
//    }

    void decodeSettings(SharedPreferences prefs) {
        String suffix = Integer.toString(getId());

        name = prefs.getString("t_sensor_name_" + suffix, "Sensor #" + suffix);
        setTargetTemperature(Float.parseFloat(prefs.getString("t_target_t_" + suffix, "25")));
    }

    void encodeSettings(SharedPreferences.Editor editor) {
        String suffix = Integer.toString(getId());

        editor.putString("t_sensor_name_" + suffix, getName());
        editor.putString("t_target_t_" + suffix, String.format(Locale.US, "%.1f°", (float) getTargetTemperature()));
    }

    @Override
    public int compareTo(@NonNull RoomSensorData o) {
        if (Integer.valueOf(this.order).equals(o.order)) {
            return Integer.valueOf(this.id).compareTo(o.id);
        } else {
            return Integer.valueOf(this.order).compareTo(o.order);
        }
    }

    public void decodeState(String payload, String type) {
        int value;

        switch (type) {
            case "temp":
                super.decodeState(payload);
                break;

            case "hum":
                char lastChar = payload.charAt(payload.length() - 1);
                if ((lastChar == '+') || (lastChar == '-')) {
                    setTemperatureTrend(lastChar);
                    payload = payload.substring(0, payload.length() - 1);
                } else
                    setTemperatureTrend('=');
                value = Integer.parseInt(payload);
                setHumidity(value);
                break;

            case "rssi":
                value = Integer.parseInt(payload);
                signalLevel = value;
                break;

            case "battery":
                batteryLevel = payload;
                break;
        }
    }
}