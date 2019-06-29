//// 0200003F2000200030Y00002733002000300Y
//package ge.altasoft.gia.cha.waterlevel;
//
//import android.annotation.SuppressLint;
//import android.content.SharedPreferences;
//
//import ge.altasoft.gia.cha.classes.RelayControllerData;
//import ge.altasoft.gia.cha.classes.TempSensorData;
//import ge.altasoft.gia.cha.other.PumpRelayData;
//import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
//
//public final class WaterLevelControllerData extends RelayControllerData {
//
////    final static int BOILER_SENSOR_SOLAR_PANEL = 0;
////    final static int BOILER_SENSOR_BOTTOM = 1;
////    final static int BOILER_SENSOR_TOP = 2;
////    final static int BOILER_SENSOR_FURNACE = 3;
//
//    final static int CLEAN_WATER_VALVE = 0;
//    final static int TECH_WATER_VALVE = 1;
//    final static int GARDEN_WATER_VALVE = 1;
//
//    final private static int TEMP_SENSOR_COUNT = 4;
//    final private static int RELAY_COUNT = 5;
//
//    final public static WaterLevelControllerData Instance = new WaterLevelControllerData();
//
//    //private final BoilerSettings boilerSettings = new BoilerSettings();
//
//    final private TempSensorData[] tempSensorsData;
//
//    @SuppressLint("UseSparseArrays")
//    private WaterLevelControllerData() {
//        super();
//
//        for (int i = 0; i < RELAY_COUNT; i++) {
//            PumpRelayData relay = new PumpRelayData(i);
//            setRelay(i, relay);
//        }
//        tempSensorsData = new BoilerSensorData[TEMP_SENSOR_COUNT];
//    }
//
//    public PumpRelayData relays(int index) {
//        return (PumpRelayData) super.relays(index);
//    }
//
//    @Override
//    public int relayCount() {
//        return RELAY_COUNT;
//    }
//
//    public TempSensorData boilerSensors(int index) {
//        return tempSensorsData[index];
//    }
//
//    void saveToPreferences(SharedPreferences prefs) {
//        SharedPreferences.Editor editor = prefs.edit();
//
//        editor.putBoolean("wl_automatic_mode", isActive());
//
//        for (int i = 0; i < RELAY_COUNT; i++)
//            relays(i).encodeSettings(editor);
//
////        for (BoilerSensorData bs : tempSensorsData)
////            bs.encodeSettings(editor);
////        boilerSettings.encodeSettings(editor);
//
//        editor.apply();
//    }
//
////    public String encodeBoilerSettings() {
////        return boilerSettings.encodeSettings();
////    }
//
////    public void decodeBoilerSettings(String response) {
////        //Log.d("decode boiler settings", response);
////
////        boilerSettings.decodeSettings(response);
////
////        haveBoilerSettings = true;
////    }
////
//
//    void decode(SharedPreferences prefs) {
//
//        setIsActive(prefs.getBoolean("wl_automatic_mode", false));
//
//        for (int i = 0; i < RELAY_COUNT; i++)
//            relays(i).decodeSettings(prefs);
//
//        //boilerSettings.decodeSettings(prefs);
//    }
//
//    //endregion
//}
