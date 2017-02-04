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
}