package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.CircularArrayList;
import ge.altasoft.gia.cha.classes.TempSensorData;

public final class RoomSensorData extends TempSensorData implements Comparable<RoomSensorData> {

    private double H;
    private String name;
    private CircularArrayList<Pair<Date, Double>> logBufferH = new CircularArrayList<>(Utils.LOG_BUFFER_SIZE);

    RoomSensorData(int id) {
        super(id);

        H = 99;
        setDeltaDesiredT(1);
    }

    public CircularArrayList<Pair<Date, Double>> getLogBufferH() {
        return logBufferH;
    }

    public String getName() {
        return this.name;
    }

    public double getHumidity() {
        return this.H;
    }

    private void setHumidity(double value)
    {
        if (this.H !=value) {
            this.H = value;
            logBufferH.add(new Pair<>(new Date(), value));
        }
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
        sb.append(String.format(Locale.US, "%02X%04X%c%04X", id, ((Double) (getTemperature() * 10)).intValue(), getTemperatureTrend(), ((Double) (H * 10)).intValue()));
    }

    int decodeState(String value, int idx) {
        setTemperature(Integer.parseInt(value.substring(idx + 2, idx + 6), 16) / 10.0);
        setTemperatureTrend(value.charAt(idx + 6));

        setHumidity(Integer.parseInt(value.substring(idx + 7, idx + 11), 16) / 10.0);
        return idx + 11;
    }

    void decodeSettings(SharedPreferences prefs) {
        String suffix = Integer.toString(getId());

        name = prefs.getString("t_sensor_name_" + suffix, "Sensor #" + suffix);
        setDesiredTemperature(Double.parseDouble(prefs.getString("t_desired_t_" + suffix, "25")));
    }

    void encodeSettings(SharedPreferences.Editor editor) {
        String suffix = Integer.toString(getId());

        editor.putString("t_sensor_name_" + suffix, getName());
        editor.putFloat("t_desired_t_" + suffix, (float)getDesiredTemperature());
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