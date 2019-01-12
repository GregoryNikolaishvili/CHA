package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;

import ge.altasoft.gia.cha.Utils;

public class BoilerSettings {

    final public static char BOILER_MODE_OFF = 'N';
    final public static char BOILER_MODE_SUMMER = 'S';
    final public static char BOILER_MODE_SUMMER_POOL = 'P';
    final public static char BOILER_MODE_WINTER = 'W';

    final public static char BOILER_MODE_SUMMER_AWAY = 's';
    final public static char BOILER_MODE_SUMMER_POOL_AWAY = 'p';
    final public static char BOILER_MODE_WINTER_AWAY = 'w';

    char Mode;

    private float CollectorSwitchOnTempDiff;
    private float CollectorSwitchOffTempDiff;

    private float CollectorEmergencySwitchOffT;
    private float CollectorEmergencySwitchOnT;
    float CollectorMinimumSwitchOnT;
    float CollectorAntifreezeT;
    private float MaxTankT;
    private float AbsoluteMaxTankT;

    private float PoolSwitchOnT;
    private float PoolSwitchOffT;

    private int BackupHeatingTS1_Start;
    private int BackupHeatingTS1_End;
    private float BackupHeatingTS1_SwitchOnT;
    float BackupHeatingTS1_SwitchOffT;

    private int BackupHeatingTS2_Start;
    private int BackupHeatingTS2_End;
    private float BackupHeatingTS2_SwitchOnT;
    private float BackupHeatingTS2_SwitchOffT;

    private int BackupHeatingTS3_Start;
    private int BackupHeatingTS3_End;
    private float BackupHeatingTS3_SwitchOnT;
    private float BackupHeatingTS3_SwitchOffT;

    BoilerSettings() {
        Mode = BOILER_MODE_OFF;
    }

    public String encodeSettings() {
        StringBuilder sb = new StringBuilder();

        sb.append(Mode);

        Utils.encodeT(sb, CollectorSwitchOnTempDiff);
        Utils.encodeT(sb, CollectorSwitchOffTempDiff);
        Utils.encodeT(sb, CollectorEmergencySwitchOffT);
        Utils.encodeT(sb, CollectorEmergencySwitchOnT);
        Utils.encodeT(sb, CollectorMinimumSwitchOnT);
        Utils.encodeT(sb, CollectorAntifreezeT);
        Utils.encodeT(sb, MaxTankT);
        Utils.encodeT(sb, AbsoluteMaxTankT);

        Utils.encodeT(sb, PoolSwitchOnT);
        Utils.encodeT(sb, PoolSwitchOffT);

        Utils.encodeTime(sb, BackupHeatingTS1_Start);
        Utils.encodeTime(sb, BackupHeatingTS1_End);
        Utils.encodeT(sb, BackupHeatingTS1_SwitchOnT);
        Utils.encodeT(sb, BackupHeatingTS1_SwitchOffT);

        Utils.encodeTime(sb, BackupHeatingTS2_Start);
        Utils.encodeTime(sb, BackupHeatingTS2_End);
        Utils.encodeT(sb, BackupHeatingTS2_SwitchOnT);
        Utils.encodeT(sb, BackupHeatingTS2_SwitchOffT);

        Utils.encodeTime(sb, BackupHeatingTS3_Start);
        Utils.encodeTime(sb, BackupHeatingTS3_End);
        Utils.encodeT(sb, BackupHeatingTS3_SwitchOnT);
        Utils.encodeT(sb, BackupHeatingTS3_SwitchOffT);

        return sb.toString();
    }

    void encodeSettings(SharedPreferences.Editor editor) {
        editor.putString("CollectorSwitchOnTempDiff", Float.toString(CollectorSwitchOnTempDiff));
        editor.putString("CollectorSwitchOffTempDiff", Float.toString(CollectorSwitchOffTempDiff));

        editor.putString("CollectorEmergencySwitchOffT", Float.toString(CollectorEmergencySwitchOffT));
        editor.putString("CollectorEmergencySwitchOnT", Float.toString(CollectorEmergencySwitchOnT));
        editor.putString("CollectorMinimumSwitchOnT", Float.toString(CollectorMinimumSwitchOnT));
        editor.putString("CollectorAntifreezeT", Float.toString(CollectorAntifreezeT));
        editor.putString("MaxTankT", Float.toString(MaxTankT));
        editor.putString("AbsoluteMaxTankT", Float.toString(AbsoluteMaxTankT));
        editor.putString("PoolSwitchOnT", Float.toString(PoolSwitchOnT));
        editor.putString("PoolSwitchOffT", Float.toString(PoolSwitchOffT));
    }

    public void decodeSettings(String response) {
        Mode = response.charAt(0);

        int idx = 1;

        CollectorSwitchOnTempDiff = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        CollectorSwitchOffTempDiff = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        CollectorEmergencySwitchOffT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        CollectorEmergencySwitchOnT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        CollectorMinimumSwitchOnT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        CollectorAntifreezeT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        MaxTankT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        AbsoluteMaxTankT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;

        PoolSwitchOnT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        PoolSwitchOffT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;

        BackupHeatingTS1_Start = Integer.parseInt(response.substring(idx, idx + 4), 16);
        idx += 4;
        BackupHeatingTS1_End = Integer.parseInt(response.substring(idx, idx + 4), 16);
        idx += 4;
        BackupHeatingTS1_SwitchOnT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        BackupHeatingTS1_SwitchOffT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;

        BackupHeatingTS2_Start = Integer.parseInt(response.substring(idx, idx + 4), 16);
        idx += 4;
        BackupHeatingTS2_End = Integer.parseInt(response.substring(idx, idx + 4), 16);
        idx += 4;
        BackupHeatingTS2_SwitchOnT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        BackupHeatingTS2_SwitchOffT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;

        BackupHeatingTS3_Start = Integer.parseInt(response.substring(idx, idx + 4), 16);
        idx += 4;
        BackupHeatingTS3_End = Integer.parseInt(response.substring(idx, idx + 4), 16);
        idx += 4;
        BackupHeatingTS3_SwitchOnT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        BackupHeatingTS3_SwitchOffT = Utils.decodeT(response.substring(idx, idx + 4));
        //idx += 4;
    }

    void decodeSettings(SharedPreferences prefs) {
        CollectorSwitchOnTempDiff = Float.parseFloat(prefs.getString("CollectorSwitchOnTempDiff", "0"));
        CollectorSwitchOffTempDiff = Float.parseFloat(prefs.getString("CollectorSwitchOffTempDiff", "0"));

        CollectorEmergencySwitchOffT = Float.parseFloat(prefs.getString("CollectorEmergencySwitchOffT", "0"));
        CollectorEmergencySwitchOnT = Float.parseFloat(prefs.getString("CollectorEmergencySwitchOnT", "0"));
        CollectorMinimumSwitchOnT = Float.parseFloat(prefs.getString("CollectorMinimumSwitchOnT", "0"));
        CollectorAntifreezeT = Float.parseFloat(prefs.getString("CollectorAntifreezeT", "0"));
        MaxTankT = Float.parseFloat(prefs.getString("MaxTankT", "0"));
        AbsoluteMaxTankT = Float.parseFloat(prefs.getString("AbsoluteMaxTankT", "0"));
        PoolSwitchOnT = Float.parseFloat(prefs.getString("PoolSwitchOnT", "0"));
        PoolSwitchOffT = Float.parseFloat(prefs.getString("PoolSwitchOffT", "0"));
    }

}