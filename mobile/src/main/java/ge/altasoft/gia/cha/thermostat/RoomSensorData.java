package ge.altasoft.gia.cha.thermostat;

import android.support.annotation.NonNull;

import java.util.Locale;

import ge.altasoft.gia.cha.Utils;

public final class RoomSensorData extends TempSensorData implements Comparable<RoomSensorData> {

    private double H;
    private String name;

    RoomSensorData(int id) {
        super(id);

        H = 99;
        setDeltaDesiredT(1);
    }

    public String getName() {
        return this.name;
    }

    public double getHumidity() {
        return this.H;
    }

    void encodeOrderAndName(StringBuilder sb2) {
        sb2.append(String.format(Locale.US, "%02X", this.id));
        sb2.append(String.format(Locale.US, "%01X", this.order));
        sb2.append(Utils.EncodeArduinoString(name));
        sb2.append(';');

    }

    void decodeOrderAndName(String s) {
        order = Character.digit(s.charAt(2), 16);
        name = Utils.DecodeArduinoString(s.substring(3));
    }

    void encodeState(StringBuilder sb) {
        sb.append(String.format(Locale.US, "%02X%04X%c%04X", id, ((Double) (getTemperature() * 10)).intValue(), temperatureTrend, ((Double) (H * 10)).intValue()));
    }

    int decodeState(String value, int idx) {
        setTemperature(Integer.parseInt(value.substring(idx + 2, idx + 6), 16) / 10.0);
        temperatureTrend = value.charAt(idx + 6);
        H = Integer.parseInt(value.substring(idx + 7, idx + 11), 16) / 10.0;

        this.lastActivitySec = System.currentTimeMillis() / 1000;

        return idx + 11;
    }


    @Override
    public int compareTo(@NonNull RoomSensorData o) {
        if (Integer.valueOf(this.order).equals(o.order)) {
            return Integer.valueOf(this.id).compareTo(o.id);
        } else {
            return Integer.valueOf(this.order).compareTo(o.order);
        }
    }
}