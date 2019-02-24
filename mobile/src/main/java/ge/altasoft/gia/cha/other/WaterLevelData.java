package ge.altasoft.gia.cha.other;

import android.content.SharedPreferences;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.TempSensorData;

public class WaterLevelData extends TempSensorData {

    private int waterDistanceCM;
    private int waterPercent;
    private boolean floatSwitchIsOn;
    private int ballValveState;
    private int ballValveSwitchState;

    private short maxDistance;
    private short minDistance;

    WaterLevelData(int id) {
        super(id);
        this.waterDistanceCM = 0;
        this.waterPercent = 0;
        this.floatSwitchIsOn = false;
        this.ballValveState = 0;
    }

    public int getWaterPercent() {
        return waterPercent;
    }

    public int getWaterDistance() {
        return waterDistanceCM;
    }

    public boolean getFloatSwitchIsOn() {
        return floatSwitchIsOn;
    }

    public int getBallValveState() {
        return ballValveState;
    }
    public int getBallValveSwitchState() {
        return ballValveSwitchState;
    }


    private void setWaterPercent(int value) {
        this.waterPercent = value;
        setLastSyncTime();
    }

    public void decodeState(String payload) {
        waterDistanceCM = Integer.parseInt(payload.substring(0, 4), 16);
        setWaterPercent(Integer.parseInt(payload.substring(4, 8), 16));
        floatSwitchIsOn = payload.charAt(8) != '0';
        ballValveState = Integer.parseInt(payload.substring(9, 13), 16);
        ballValveSwitchState = Integer.parseInt(payload.substring(13, 15), 16);
    }

    @Override
    public int decodeSettings(String response, int idx) {
        maxDistance = Short.parseShort(response.substring(idx, idx + 4), 16);
        minDistance = Short.parseShort(response.substring(idx + 4, idx + 8), 16);

        idx += 8;
        return idx;
    }

    @Override
    public void encodeSettings(StringBuilder sb) {
        sb.append(Utils.shortToHex4(maxDistance));
        sb.append(Utils.shortToHex4(minDistance));
    }

    void decodeSettings(SharedPreferences prefs) {
        String suffix = Integer.toString(getId());

        maxDistance = Short.parseShort(prefs.getString("wl_max_" + suffix, "0"));
        minDistance = Short.parseShort(prefs.getString("wl_min_" + suffix, "0"));
    }

    void encodeSettings(SharedPreferences.Editor editor) {
        String suffix = Integer.toString(getId());

        editor.putString("wl_max_" + suffix, Integer.toString(maxDistance));
        editor.putString("wl_min_" + suffix, Integer.toString(minDistance));
    }
}