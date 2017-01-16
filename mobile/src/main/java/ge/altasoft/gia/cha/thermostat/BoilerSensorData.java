package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;

import java.util.Locale;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.TempSensorData;


public final class BoilerSensorData extends TempSensorData {

    BoilerSensorData(int id) {
        super(id);

        setDeltaDesiredT(5);
    }

    void encodeState(StringBuilder sb) {
        if (Utils.DEBUG_THERMOSTAT) {
            int trend = Utils.random(0, 2);
            char c = NO_CHANGE;
            if (trend == 1)
                c = GOING_UP;
            else if (trend == 2)
                c = GOING_DOWN;

            sb.append(String.format(Locale.US, "%04X%c", Utils.random(10, 100) * 10, c));
        } else
            sb.append(String.format(Locale.US, "%04X%c", ((Double) (getTemperature() * 10)).intValue(), getTemperatureTrend()));
    }

    int decodeState(String value, int idx) {
        setTemperature(Integer.parseInt(value.substring(idx, idx + 4), 16) / 10.0);
        setTemperatureTrend(value.charAt(idx + 4));

        return idx + 5;
    }


    public void encodeSettings(StringBuilder sb) {
    }


    void decodeSettings(SharedPreferences prefs) {
        //String suffix = Integer.toString(getId());

        //name = prefs.getString("t_sensor_name_" + suffix, "Sensor #" + suffix);
        //setDesiredTemperature(Double.parseDouble(prefs.getString("t_desired_t_" + suffix, "25")));
    }

    public void encodeSettings(SharedPreferences.Editor editor) {
        //String suffix = Integer.toString(getId());

        //editor.putString("t_bsensor_name_" + suffix, getName());
        //editor.putFloat("t_desired_t_" + suffix, (float)getDesiredTemperature());
    }
}