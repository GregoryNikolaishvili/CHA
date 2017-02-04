package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.TempSensorData;

public final class RoomSensorData extends TempSensorData implements Comparable<RoomSensorData> {

    private float H;
    private int signalLevel;
    private String batteryLevel;
    private String name;
    private boolean isOn;
    private int responsibleRelayId;
    private boolean isDeleted;

    RoomSensorData(int id) {
        super(id);

        H = Float.NaN;
        setDeltaTargetT(1.0f);
        this.isOn = false;
        responsibleRelayId = 0; // no responsible
        signalLevel = 0;
        batteryLevel = "unknown";
        name = String.valueOf(id);
        isDeleted = false;
    }

    public boolean isOn() {
        return this.isOn;
    }

    public boolean hasRelay() {
        return this.responsibleRelayId > 0;
    }

    boolean isDeleted() {
        return this.isDeleted;
    }

    public void setIsOn(boolean value) {
        this.isOn = value;
    }

    public String getName() {
        return this.name;
    }

    public float getHumidity() {
        return this.H;
    }

    private void setHumidity(float value) {
        if (value == Utils.F_UNDEFINED)
            this.H = Float.NaN;
        else
            this.H = value;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    int getResponsibleRelayId() {
        return this.responsibleRelayId;
    }

    void encodeOrderAndName(StringBuilder sb2) {
        sb2.append(String.format(Locale.US, "%04X", this.id));
        sb2.append(String.format(Locale.US, "%01X", this.order));
        sb2.append(Utils.encodeArduinoString(name));
        sb2.append(';');
    }

    void decodeOrderAndName(String s) {
        order = Character.digit(s.charAt(0), 16);
        name = Utils.decodeArduinoString(s.substring(1));
    }

    public void encodeSettings(StringBuilder sb) {
        super.encodeSettings(sb);
        sb.append(String.format(Locale.US, "%02X", responsibleRelayId));
    }

    public int decodeSettings(String response, int idx) {
        idx = super.decodeSettings(response, idx);
        responsibleRelayId = Integer.parseInt(response.substring(idx, idx + 2), 16);
        return idx + 2;
    }

    void decodeSettings(SharedPreferences prefs) {
        String suffix = Integer.toString(getId());

        name = prefs.getString("t_sensor_name_".concat(suffix), "Sensor #" + suffix);
        setTargetTemperature(Float.parseFloat(prefs.getString("t_target_t_".concat(suffix), "25")));
        responsibleRelayId = Integer.parseInt(prefs.getString("t_resp_relay_id_".concat(suffix), "0"));
        isDeleted = prefs.getBoolean("t_sensor_deleted_Sensor #".concat(suffix), false);
    }

    void encodeSettings(SharedPreferences.Editor editor) {
        String suffix = Integer.toString(getId());

        editor.putString("t_sensor_name_".concat(suffix), getName());
        editor.putString("t_target_t_".concat(suffix), String.format(Locale.US, "%.1f", (float) getTargetTemperature()));
        editor.putString("t_resp_relay_id_".concat(suffix), String.valueOf(responsibleRelayId));
        editor.putBoolean("t_sensor_deleted_Sensor #".concat(suffix), false);
    }

    @Override
    public int compareTo(@NonNull RoomSensorData o) {
        if (Integer.valueOf(this.order).equals(o.order)) {
            return Integer.valueOf(this.id).compareTo(o.id);
        } else {
            return Integer.valueOf(this.order).compareTo(o.order);
        }
    }

    @Override
    public void decodeState(String payload) {
        JSONObject jMain;
        try {
            jMain = new JSONObject(payload);
            setTemperature(jMain.getInt("T") / 10f);
            setTemperatureTrend(jMain.getString("TT").charAt(0));

            setHumidity(jMain.getInt("H"));
            signalLevel = jMain.getInt("S");
            batteryLevel = jMain.getString("B");
        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
        }
    }
}