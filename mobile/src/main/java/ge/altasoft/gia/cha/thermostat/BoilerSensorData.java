package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;

import java.util.Locale;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.TempSensorData;


public final class BoilerSensorData extends TempSensorData {

    private float targetSummerT = Utils.DEFAULT_TARGET_TEMPERATURE;
    private float targetSummerAndPoolT = Utils.DEFAULT_TARGET_TEMPERATURE;
    private float targetWinterT = Utils.DEFAULT_TARGET_TEMPERATURE;

    BoilerSensorData(int id) {
        super(id);

        setDeltaTargetT(5f);
    }

    public void encodeSettings(StringBuilder sb) {
        super.encodeSettings(sb);
        sb.append(String.format(Locale.US, "%04X%04X%04X", ((Float) (targetSummerT * 10)).intValue(), ((Float) (targetSummerAndPoolT * 10)).intValue(), ((Float) (targetWinterT * 10)).intValue()));
    }

    public int decodeSettings(String response, int idx) {
        idx = super.decodeSettings(response, idx);

        targetSummerT = Integer.parseInt(response.substring(idx, idx + 4), 16) / 10.0f;
        targetSummerAndPoolT = Integer.parseInt(response.substring(idx, idx + 4), 16) / 10.0f;
        targetWinterT = Integer.parseInt(response.substring(idx, idx + 4), 16) / 10.0f;

        return idx + 12;
    }

    void decodeSettings(SharedPreferences prefs) {
        //String suffix = Integer.toString(getId());

        //name = prefs.getString("t_sensor_name_" + suffix, "Sensor #" + suffix);
        //setTargetTemperature(Float.parseFloat(prefs.getString("t_target_t_" + suffix, "25")));
    }

    public void encodeSettings(SharedPreferences.Editor editor) {
        //String suffix = Integer.toString(getId());

        //editor.putString("t_sensor_name_" + suffix, getName());
        //editor.putFloat("t_target_t_" + suffix, (float)getTargetTemperature());
    }
}