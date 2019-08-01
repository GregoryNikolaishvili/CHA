package ge.altasoft.gia.cha.other;

import android.content.Context;
import android.content.SharedPreferences;

import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.RelayControllerData;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.views.PressureSensorView;
import ge.altasoft.gia.cha.views.WlPumpRelayView;
import ge.altasoft.gia.cha.views.RainSensorView;
import ge.altasoft.gia.cha.views.WaterLevelSensorView;
import ge.altasoft.gia.cha.views.WindDirSensorView;
import ge.altasoft.gia.cha.views.WindSensorView;

public final class OtherControllerData extends RelayControllerData {

    //final static public int _5IN1_SENSOR_ID_TH = 0;
    final static public int _5IN1_SENSOR_ID_WIND = 0;
    final static public int _5IN1_SENSOR_ID_PRESSURE = 1;
    final static public int _5IN1_SENSOR_ID_RAIN = 2;
    final static public int _5IN1_SENSOR_ID_WIND_DIR = 3;

    final static private int _WATER_LEVEL_SENSOR1 = 4;
    final static private int _WATER_LEVEL_SENSOR2 = 5;
    final static private int _WATER_LEVEL_SENSOR3 = 6;

    final static private int _WATER_LEVEL_RELAY1 = 7;
    final static private int _WATER_LEVEL_RELAY2 = 8;
    final static private int _WATER_LEVEL_RELAY3 = 9;
    final static private int _WATER_LEVEL_RELAY4 = 10;
    final static private int _WATER_LEVEL_RELAY5 = 11;

    final static int RELAY_COUNT = 5;

    final static private int SENSOR_COUNT = 4 + 3 + RELAY_COUNT;

    final public static OtherControllerData Instance = new OtherControllerData();

    private final Sensor5in1Data sensor5in1Data;
    private final WaterLevelData[] waterLevelDatas;

    private OtherControllerData() {

        sensor5in1Data = new Sensor5in1Data(0); // 5 sensors
        waterLevelDatas = new WaterLevelData[3];
        waterLevelDatas[0] = new WaterLevelData(0);
        waterLevelDatas[1] = new WaterLevelData(1);
        waterLevelDatas[2] = new WaterLevelData(2);

        for (int i = 0; i < RELAY_COUNT; i++) {
            RelayData relay = new WlPumpRelayData(i);
            setRelay(i, relay);
        }
    }

    @Override
    public int relayCount() {
        return RELAY_COUNT;
    }

    public WlPumpRelayData relays(int index) {
        return (WlPumpRelayData) super.relays(index);
    }

    int sensorCount() {
        return SENSOR_COUNT;
    }

    ChaWidget createWidget(Context context, int position, boolean fromDashboard) {
        switch (position) {
            case _5IN1_SENSOR_ID_WIND:
                return new WindSensorView(context, fromDashboard);
            case _5IN1_SENSOR_ID_PRESSURE:
                return new PressureSensorView(context, fromDashboard);
            case _5IN1_SENSOR_ID_RAIN:
                return new RainSensorView(context, fromDashboard);
            case _5IN1_SENSOR_ID_WIND_DIR:
                return new WindDirSensorView(context, fromDashboard);
            case _WATER_LEVEL_SENSOR1:
            case _WATER_LEVEL_SENSOR2:
            case _WATER_LEVEL_SENSOR3:
                WaterLevelData sd = OtherControllerData.Instance.getWaterLevelData(position - _WATER_LEVEL_SENSOR1);
                WaterLevelSensorView w = new WaterLevelSensorView(context, fromDashboard);
                w.setWaterLevelData(sd);
                return w;
            case _WATER_LEVEL_RELAY1:
            case _WATER_LEVEL_RELAY2:
            case _WATER_LEVEL_RELAY3:
            case _WATER_LEVEL_RELAY4:
            case _WATER_LEVEL_RELAY5:
                RelayData rd = OtherControllerData.Instance.relays(position - _WATER_LEVEL_RELAY1);
                WlPumpRelayView rw = new WlPumpRelayView(context, fromDashboard);
                rw.setRelayData(rd);
                return rw;
            default:
                return null;
        }
    }

    public Sensor5in1Data get5in1SensorData() {
        return this.sensor5in1Data;
    }

    public WaterLevelData getWaterLevelData(int id) {
        if ((id >= 0) && (id < 3))
            return waterLevelDatas[id];
        else
            return null;
    }

    public void decodeWaterLevelSettings(String payload) {

        int idx = 0;
        idx = waterLevelDatas[0].decodeSettings(payload, idx);
        idx = waterLevelDatas[1].decodeSettings(payload, idx);
        waterLevelDatas[2].decodeSettings(payload, idx);
    }

    public String encodeWaterLevelSettings() {
        StringBuilder sb = new StringBuilder();

        waterLevelDatas[0].encodeSettings(sb);
        waterLevelDatas[1].encodeSettings(sb);
        waterLevelDatas[2].encodeSettings(sb);

        return sb.toString();
    }

    void decode(SharedPreferences prefs) {

        setIsActive(prefs.getBoolean("wl_automatic_mode", false));

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).decodeSettings(prefs);

        waterLevelDatas[0].decodeSettings(prefs);
        waterLevelDatas[1].decodeSettings(prefs);
        waterLevelDatas[2].decodeSettings(prefs);
    }

    void saveToPreferences(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("wl_automatic_mode", isActive());

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).encodeSettings(editor);

        waterLevelDatas[0].encodeSettings(editor);
        waterLevelDatas[1].encodeSettings(editor);
        waterLevelDatas[2].encodeSettings(editor);

        editor.apply();
    }

}
