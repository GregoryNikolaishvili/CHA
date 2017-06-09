package ge.altasoft.gia.cha.thermostat;

import ge.altasoft.gia.cha.Utils;

class BoilerSettings {

    final private static char BOILER_MODE_OFF = 'N';
    final static char BOILER_MODE_SUMMER = 'S';
    final static char BOILER_MODE_SUMMER_POOL = 'P';
    final static char BOILER_MODE_WINTER = 'W';


    char Mode;

    float CollectorSwitchOnTempDiff;
    float CollectorSwitchOffTempDiff;

    float EmergencyCollectorSwitchOffT;
    float EmergencyCollectorSwitchOnT;
    float CollectorCoolingT;
    float MaxTankT;

    float PoolSwitchOnT;
    float PoolSwitchOffT;

    int BackupHeatingTS1_Start;
    int BackupHeatingTS1_End;
    float BackupHeatingTS1_SwitchOnT;
    float BackupHeatingTS1_SwitchOffT;

    int BackupHeatingTS2_Start;
    int BackupHeatingTS2_End;
    float BackupHeatingTS2_SwitchOnT;
    float BackupHeatingTS2_SwitchOffT;

    int BackupHeatingTS3_Start;
    int BackupHeatingTS3_End;
    float BackupHeatingTS3_SwitchOnT;
    float BackupHeatingTS3_SwitchOffT;

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

        Utils.encodeTime(sb, BackupHeatingTS2_Start);
        Utils.encodeTime(sb, BackupHeatingTS2_End);
        Utils.encodeT(sb, BackupHeatingTS2_SwitchOnT);
        Utils.encodeT(sb, BackupHeatingTS2_SwitchOffT);

        return sb.toString();
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
}