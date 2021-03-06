// 0200003F2000200030Y00002733002000300Y
package ge.altasoft.gia.cha.thermostat;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import ge.altasoft.gia.cha.classes.RelayControllerData;

import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER;
import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER_AWAY;
import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER_POOL;
import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER_POOL_AWAY;
import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_WINTER;
import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_WINTER_AWAY;

public final class ThermostatControllerData extends RelayControllerData {

    final static int BOILER_SENSOR_SOLAR_PANEL = 0;
    final static int BOILER_SENSOR_BOTTOM = 1;
    final static int BOILER_SENSOR_TOP = 2;
    final static int BOILER_SENSOR_FURNACE = 3;

    final static int BOILER_SOLAR_PUMP = 0;
    final static int BOILER_HEATING_PUMP = 1;
    final static int BOILER_FURNACE = 2;
    //final static int BOILER_FURNACE_CIRC_PUMP = 3;

    final static private int HEATING_RELAY_COUNT = 15;

    final private static int BOILER_SENSOR_COUNT = 4;
    final private static int BOILER_PUMP_COUNT = 4;

    final public static ThermostatControllerData Instance = new ThermostatControllerData();

    private final BoilerSettings boilerSettings = new BoilerSettings();

    final private BoilerSensorData[] boilerSensorsData;
    final private BoilerPumpData[] boilerPumpsData;

    final private HashMap<Integer, RoomSensorData> roomSensorsMap;

    private boolean haveRoomSensorsSettings;
    private boolean haveBoilerSettings;

    @SuppressLint("UseSparseArrays")
    private ThermostatControllerData() {
        super();

        haveRoomSensorsSettings = false;
        haveBoilerSettings = false;

        boilerSensorsData = new BoilerSensorData[BOILER_SENSOR_COUNT];
        boilerPumpsData = new BoilerPumpData[BOILER_PUMP_COUNT];

        roomSensorsMap = new HashMap<>();

        for (int i = 0; i < BOILER_SENSOR_COUNT; i++)
            boilerSensorsData[i] = new BoilerSensorData(i);

        for (int i = 0; i < BOILER_PUMP_COUNT; i++)
            boilerPumpsData[i] = new BoilerPumpData(i);

        boilerPumpsData[BOILER_SOLAR_PUMP].setName("Solar pump");
        boilerPumpsData[BOILER_HEATING_PUMP].setName("Heating pump");

        for (int i = 0; i < HEATING_RELAY_COUNT; i++) {
            ThermostatRelayData relay = new ThermostatRelayData(i);
            setRelay(i, relay);
        }
    }

    @Override
    public int relayCount() {
        return HEATING_RELAY_COUNT;
    }

    int roomSensorCount() {
        return roomSensorsMap.size();
    }

    boolean haveRoomSensorsSettings() {
        return this.haveRoomSensorsSettings;
    }

    public boolean haveBoilerSettings() {
        return this.haveBoilerSettings;
    }


    public ArrayList<Integer> setHeaterRelayValue(int relayId, int value) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (RoomSensorData ss : roomSensorsMap.values()) {
            if (ss.getResponsibleRelayId() == relayId) {
                ss.setValue(value);
                ids.add(ss.getId());
            }
        }
        return ids;
    }

    public ThermostatRelayData relays(int index) {
        return (ThermostatRelayData) super.relays(index);
    }

    public BoilerPumpData boilerPumps(int index) {
        return boilerPumpsData[index];
    }

    public BoilerSensorData boilerSensors(int index) {
        return boilerSensorsData[index];
    }

    Map<Integer, RoomSensorData> sortedRoomSensors() {

        return sortByOrder(roomSensorsMap);
    }

    public RoomSensorData roomSensors(int id, boolean createNewIfNotFound) {
        RoomSensorData roomSensorData = roomSensorsMap.get(id);

        if (createNewIfNotFound && (roomSensorData == null)) {
            roomSensorData = new RoomSensorData(id);
            roomSensorsMap.put(id, roomSensorData);
        }

        return roomSensorData;
    }

    RoomSensorData getRoomSensorFromUIIndex(int index) {

        Map<Integer, RoomSensorData> ss = sortByOrder(roomSensorsMap);

        int idx = 0;
        for (int id : ss.keySet()) {
            if (idx == index)
                return roomSensorsMap.get(id);
            idx++;
        }
        return null;
    }

    void saveToPreferences(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("t_boiler_mode", String.valueOf(getBoilerMode()));

        for (int i = 0; i < HEATING_RELAY_COUNT; i++)
            relays(i).encodeSettings(editor);

        for (RoomSensorData ss : roomSensorsMap.values())
            ss.encodeSettings(editor);

//        for (BoilerSensorData bs : boilerSensorsData)
//            bs.encodeSettings(editor);

        boilerSettings.encodeSettings(editor);

        editor.apply();
    }

    public char getBoilerMode() {
        return boilerSettings.Mode;
    }

    private void setBoilerMode(char mode) {
        boilerSettings.Mode = mode;
    }

    String getBoilerModeText() {
        switch (boilerSettings.Mode) {
            case BOILER_MODE_SUMMER:
                return "Summer";
            case BOILER_MODE_SUMMER_AWAY:
                return "Summer (away)";
            case BOILER_MODE_SUMMER_POOL:
                return "Summer & Pool";
            case BOILER_MODE_SUMMER_POOL_AWAY:
                return "Summer & Pool (away)";
            case BOILER_MODE_WINTER:
                return "Winter";
            case BOILER_MODE_WINTER_AWAY:
                return "Winter (away)";
            default:
                return "Off";
        }
    }

//    private char nextBoilerMode() {
//        switch (boilerSettings.Mode) {
//            case BOILER_MODE_OFF:
//                return BOILER_MODE_SUMMER;
//            case BOILER_MODE_SUMMER:
//                return BOILER_MODE_SUMMER_POOL;
//            case BOILER_MODE_SUMMER_POOL:
//                return BOILER_MODE_WINTER;
//            case BOILER_MODE_WINTER:
//                return BOILER_MODE_OFF;
//            default:
//                return BOILER_MODE_SUMMER;
//        }
//    }

    //region Encode/Decode
    public String encodeRoomSensorSettings() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format(Locale.US, "%02X", roomSensorsMap.size()));
        for (int id : roomSensorsMap.keySet()) {
            sb.append(String.format(Locale.US, "%04X", id));
            roomSensorsMap.get(id).encodeSettings(sb);
        }

        return sb.toString();
    }

    public String encodeBoilerSettings() {
        return boilerSettings.encodeSettings();
    }

    public String encodeRoomSensorNamesAndOrder() {
        StringBuilder sb = new StringBuilder();

        for (int id : roomSensorsMap.keySet())
            roomSensorsMap.get(id).encodeOrderAndName(sb);
        sb.insert(0, String.format(Locale.US, "%04X", sb.length()));

        if (sb.length() > 256)
            sb.setLength(256);
        return sb.toString();
    }

    public void decodeRoomSensorSettings(String response) {
        //Log.d("decode sensor settings", response);

        int count = Integer.parseInt(response.substring(0, 2), 16);

        int idx = 2;
        for (int i = 0; i < count; i++) {
            int id = Integer.parseInt(response.substring(idx, idx + 4), 16);

            RoomSensorData roomSensorData = roomSensors(id, true);
            idx = roomSensorData.decodeSettings(response, idx + 4);
        }

        haveRoomSensorsSettings = true;
    }

    public void decodeRoomSensorNamesAndOrder(String response) {
        //Log.d("decode sensor names", response);

        response = response.substring(4); // first 4 digits is length in hex

        String[] parts = response.split(";");

        for (String s : parts) {
            int id = Integer.parseInt(s.substring(0, 4), 16);

            RoomSensorData roomSensorData = roomSensors(id, true);
            roomSensorData.decodeOrderAndName(s.substring(4));
        }
    }

    public void decodeBoilerSettings(String response) {
        //Log.d("decode boiler settings", response);

        boilerSettings.decodeSettings(response);

        haveBoilerSettings = true;
    }


    void decode(SharedPreferences prefs) {

        setBoilerMode(prefs.getString("t_boiler_mode", "N").charAt(0));

        for (int i = 0; i < HEATING_RELAY_COUNT; i++)
            relays(i).decodeSettings(prefs);

        for (int id : roomSensorsMap.keySet())
            roomSensors(id, false).decodeSettings(prefs);

        boilerSettings.decodeSettings(prefs);

        Iterator<Integer> it = roomSensorsMap.keySet().iterator();
        while (it.hasNext()) {
            int id = it.next();
            if (roomSensorsMap.get(id).isDeleted())
                it.remove();
        }
    }

    //endregion
}
