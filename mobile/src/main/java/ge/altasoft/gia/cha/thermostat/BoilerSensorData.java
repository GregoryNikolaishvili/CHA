package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;

import java.util.Locale;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.TempSensorData;


public final class BoilerSensorData extends TempSensorData {

    BoilerSensorData(int id) {
        super(id);

        setDeltaTargetT(5f);
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