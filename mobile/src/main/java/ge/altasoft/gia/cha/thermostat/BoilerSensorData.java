package ge.altasoft.gia.cha.thermostat;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.TempSensorData;


public final class BoilerSensorData extends TempSensorData {

    BoilerSensorData(int id) {
        super(id);

        setDeltaTargetT(5f);
    }

    public void decodeState(String payload) {
        setTemperature(Utils.decodeT(payload.substring(0, 4)));
        setTemperatureTrend(payload.charAt(4));
        setLastSyncTime(Integer.parseInt(payload.substring(5, 9), 16));
    }

}