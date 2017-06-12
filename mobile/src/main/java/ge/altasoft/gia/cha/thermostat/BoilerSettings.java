package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;

import ge.altasoft.gia.cha.Utils;

class BoilerSettings {

    final private static char BOILER_MODE_OFF = 'N';
    final static char BOILER_MODE_SUMMER = 'S';
    final static char BOILER_MODE_SUMMER_POOL = 'P';
    final static char BOILER_MODE_WINTER = 'W';

    char Mode;

    private float CollectorSwitchOnTempDiff;
    private float CollectorSwitchOffTempDiff;

    private float EmergencyCollectorSwitchOffT;
    private float EmergencyCollectorSwitchOnT;
    float CollectorCoolingT;
    private float MaxTankT;

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
        Utils.encodeT(sb, EmergencyCollectorSwitchOffT);
        Utils.encodeT(sb, EmergencyCollectorSwitchOnT);
        Utils.encodeT(sb, CollectorCoolingT);
        Utils.encodeT(sb, MaxTankT);

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

        editor.putString("EmergencyCollectorSwitchOffT", Float.toString(EmergencyCollectorSwitchOffT));
        editor.putString("EmergencyCollectorSwitchOnT", Float.toString(EmergencyCollectorSwitchOnT));
        editor.putString("CollectorCoolingT", Float.toString(CollectorCoolingT));
        editor.putString("MaxTankT", Float.toString(MaxTankT));
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
        EmergencyCollectorSwitchOffT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        EmergencyCollectorSwitchOnT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        CollectorCoolingT = Utils.decodeT(response.substring(idx, idx + 4));
        idx += 4;
        MaxTankT = Utils.decodeT(response.substring(idx, idx + 4));
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

        EmergencyCollectorSwitchOffT = Float.parseFloat(prefs.getString("EmergencyCollectorSwitchOffT", "0"));
        EmergencyCollectorSwitchOnT= Float.parseFloat(prefs.getString("EmergencyCollectorSwitchOnT", "0"));
        CollectorCoolingT= Float.parseFloat(prefs.getString("CollectorCoolingT", "0"));
        MaxTankT= Float.parseFloat(prefs.getString("MaxTankT", "0"));
        PoolSwitchOnT= Float.parseFloat(prefs.getString("PoolSwitchOnT", "0"));
        PoolSwitchOffT= Float.parseFloat(prefs.getString("PoolSwitchOffT", "0"));
    }

}