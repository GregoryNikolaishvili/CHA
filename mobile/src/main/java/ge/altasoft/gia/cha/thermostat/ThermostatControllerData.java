package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseIntArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import ge.altasoft.gia.cha.classes.RelayControllerData;
import ge.altasoft.gia.cha.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ThermostatControllerData extends RelayControllerData {

    final static int BOILER_SENSOR_SOLAR_PANEL = 0;
    final static int BOILER_SENSOR_BOTTOM = 1;
    final static int BOILER_SENSOR_TOP = 2;
    final static int BOILER_SENSOR_ROOM = 3;

    final static int BOILER_SOLAR_PUMP = 0;
    final static int BOILER_HEATING_PUMP = 1;

    final static int RELAY_COUNT = 15;
    final public static int BOILER_SENSOR_COUNT = 4;
    final private static int BOILER_PUMP_COUNT = 2;

    final public static ThermostatControllerData Instance = new ThermostatControllerData();

    private BoilerSensorData[] boilerSensorsData;
    private BoilerPumpData[] boilerPumpsData;

    private HashMap<Integer, RoomSensorData> roomSensorsMap;
    private SparseIntArray savedRoomSensorOrders;
    private boolean roomSensorsReordered;

    private ThermostatControllerData() {
        super();

        roomSensorsReordered = false;

        boilerSensorsData = new BoilerSensorData[BOILER_SENSOR_COUNT];
        boilerPumpsData = new BoilerPumpData[BOILER_PUMP_COUNT];

        roomSensorsMap = new HashMap<>();
        savedRoomSensorOrders = new SparseIntArray();

        for (int i = 0; i < BOILER_SENSOR_COUNT; i++)
            boilerSensorsData[i] = new BoilerSensorData(i + 1);

        for (int i = 0; i < BOILER_PUMP_COUNT; i++)
            boilerPumpsData[i] = new BoilerPumpData(i + 1);

        boilerPumpsData[BOILER_SOLAR_PUMP].setName("Solar pump");
        boilerPumpsData[BOILER_HEATING_PUMP].setName("Heating pump");

        for (int i = 0; i < RELAY_COUNT; i++) {
            ThermostatRelayData relay = new ThermostatRelayData(i + 1);
            setRelay(i, relay);
        }
    }

    @Override
    public int relayCount() {
        return RELAY_COUNT;
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

    public RoomSensorData roomSensors(int id) {
        return roomSensorsMap.get(id);
    }

    public void saveRoomSensorOrders() {
        roomSensorsReordered = false;

        savedRoomSensorOrders.clear();
        for (int id : roomSensorsMap.keySet())
            savedRoomSensorOrders.append(id, roomSensorsMap.get(id).getOrder());
    }

    public void restoreRoomSensorRelayOrders() {
        for (int id : roomSensorsMap.keySet())
            roomSensorsMap.get(id).setOrder(savedRoomSensorOrders.get(id));

        roomSensorsReordered = false;
        savedRoomSensorOrders.clear();
    }

    public boolean roomSensorOrderChanged() {
        return roomSensorsReordered;
    }

    private RoomSensorData getRoomSensorFromUIIndex(int index) {

        Map<Integer, RoomSensorData> ss = sortByOrder(roomSensorsMap);

        int idx = 0;
        for (int id : ss.keySet()) {
            if (idx == index)
                return roomSensorsMap.get(id);
            idx++;
        }
        return null;
    }

    void reorderRoomSensorMapping(int firstIndex, int secondIndex) {

        RoomSensorData firstSensor = getRoomSensorFromUIIndex(firstIndex);
        RoomSensorData secondSensor = getRoomSensorFromUIIndex(secondIndex);

        if ((firstSensor == null) || (secondSensor == null))
            return;

        int order = firstSensor.getOrder();

        firstSensor.setOrder(secondSensor.getOrder());
        secondSensor.setOrder(order);

        roomSensorsReordered = true;
    }

    JSONObject encodeState() {
        StringBuilder sb = new StringBuilder();

        JSONObject jState = new JSONObject();
        try {
            if (haveSettings()) {
                // Relays
                sb.append(isActive() ? 'T' : 'F');
                for (int i = 0; i < RELAY_COUNT; i++)
                    relays(i).encodeState(sb);
                jState.put("relays", sb.toString());

                // Room sensors
                sb.setLength(0);
                if (Utils.DEBUG_THERMOSTAT) {
                    sb.append(String.format(Locale.US, "%02X", 10));
                    for (int id = 1; id <= 10; id++) {
                        int trend = Utils.random(0, 2);
                        char c = RoomSensorData.NO_CHANGE;
                        if (trend == 1)
                            c = RoomSensorData.GOING_UP;
                        else if (trend == 2)
                            c = RoomSensorData.GOING_DOWN;
                        sb.append(String.format(Locale.US, "%02X%04X%c%04X", id, Utils.random(10, 100) * 10, c, Utils.random(10, 100) * 10));
                    }
                } else {
                    sb.append(String.format(Locale.US, "%02X", roomSensorsMap.size()));
                    for (int id : roomSensorsMap.keySet()) {
                        roomSensorsMap.get(id).encodeState(sb);
                    }
                }
                jState.put("sensors", sb.toString());

                // boiler sensors
                sb.setLength(0);

                for (int i = 0; i < BOILER_SENSOR_COUNT; i++)
                    boilerSensorsData[i].encodeState(sb);

                // boiler relays
                for (int i = 0; i < BOILER_PUMP_COUNT; i++)
                    //boilerPumpsData[i].encodeState(sb);
                    sb.append((new Random()).nextBoolean() ? '1' : '0');

                jState.put("boiler", sb.toString());
            } else {
                // Relays
                sb.append(isActive() ? 'T' : 'F');
                for (int i = 0; i < RELAY_COUNT; i++)
                    sb.append('0');
                jState.put("relays", sb.toString());

                // Room sensors
                jState.put("sensors", "00");

                // boiler sensors
                sb.setLength(0);
                for (int i = 0; i < BOILER_SENSOR_COUNT; i++)
                    sb.append(String.format(Locale.US, "%04XN", (i + 1) * 100));
                // boiler relays
                for (int i = 0; i < BOILER_PUMP_COUNT; i++)
                    sb.append('0');
                jState.put("boiler", sb.toString());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
            jState.put("time", sdf.format(getNow()));
        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
        }

        return jState;
    }

    private String encodeRelaySettings() {
        StringBuilder sb = new StringBuilder();

        sb.append(isActive() ? 'T' : 'F');
        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).encodeSettings(sb);

        sb.append('*');
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < RELAY_COUNT; i++) {
            if (haveSettings())
                relays(i).encodeOrderAndName(sb2);
            else
                ThermostatRelayData.encodeOrderAndNameDebug(sb2, i);
        }
        sb2.insert(0, String.format(Locale.US, "%04X", sb2.length()));

        sb.append(sb2);

        return sb.toString();
    }

    private String encodeRoomSensorSettings() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        if (haveSettings()) {
            sb.append(String.format(Locale.US, "%02X*", roomSensorsMap.size()));
            for (int id : roomSensorsMap.keySet())
                roomSensorsMap.get(id).encodeSettings(sb);

            sb.append('*');
            for (int id : roomSensorsMap.keySet())
                roomSensorsMap.get(id).encodeOrderAndName(sb2);
        } else {
            if (Utils.DEBUG_THERMOSTAT) {
                sb.append(String.format(Locale.US, "%02X*", 10));
                for (int id = 1; id <= 10; id++) {
                    sb.append(String.format(Locale.US, "%02X", id));
                    sb.append(id == 3 ? 'N' : 'C');
                }

                sb.append('*');
                for (int id = 1; id <= 10; id++) {
                    sb2.append(String.format(Locale.US, "%02X%01X", id, id));
                    sb2.append(Utils.EncodeArduinoString("Sensor #" + id));
                    sb2.append(';');
                }
            } else
                sb2.append(String.format(Locale.US, "%02X*", 0));
        }

        sb.append(sb2);

        return sb.toString();
    }

    private String encodeBoilerSettings() {
        StringBuilder sb = new StringBuilder();

        //sb.append(isActive() ? 'T' : 'F');
        for (int i = 0; i < BOILER_SENSOR_COUNT; i++)
            boilerSensors(i).encodeSettings(sb);

        sb.append('*');
        for (int i = 0; i < BOILER_PUMP_COUNT; i++)
            boilerPumps(i).encodeSettings(sb);

        return sb.toString();
    }

    public String encodeSettings() {

        JSONObject jMain = new JSONObject();
        try {
            JSONObject jSettings = new JSONObject();
            jSettings.put("relays", encodeRelaySettings());
            jSettings.put("sensors", encodeRoomSensorSettings());
            jSettings.put("boiler", encodeBoilerSettings());

            jMain.put("settings", jSettings);
        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
        }

        return "*" + jMain.toString();
    }

    private boolean decodeRelaySettings(String response) {
        if (response == null)
            return false;

        int idx = 0;
        setIsActive(response.charAt(idx++) != 'F');
        for (int i = 0; i < RELAY_COUNT; i++)
            idx = relays(i).decodeSettings(response, idx);

        if (response.charAt(idx++) != '*') {
            Log.e("ThermControllerData", "Not '*'");
            return false;
        }

        //int length = Integer.parseInt(response.substring(idx, idx + 4), 16);
        idx += 4;
        response = response.substring(idx);

        String[] arr = response.split(";");
        if (arr.length < RELAY_COUNT) {
            Log.e("ThermControllerData", "Invalid number of relays returned");
            return false;
        }

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).decodeOrderAndName(arr[i]);

        return true;
    }

    private boolean decodeRoomSensorSettings(String response) {
        if (response == null)
            return false;

        int count = Integer.parseInt(response.substring(0, 2), 16);

        int idx = 2;
        if (response.charAt(idx++) != '*') {
            Log.e("ThermControllerData", "Not '*'");
            return false;
        }

        for (int i = 0; i < count; i++) {
            int id = Integer.parseInt(response.substring(idx, idx + 2), 16);

            RoomSensorData roomSensorData = roomSensorsMap.get(id);
            if (roomSensorData == null) {
                roomSensorData = new RoomSensorData(id);
                roomSensorsMap.put(id, roomSensorData);
            }

            idx = roomSensorData.decodeSettings(response, idx + 2);
        }

        if (response.charAt(idx++) != '*') {
            Log.e("ThermControllerData", "Not '*'");
            return false;
        }

        response = response.substring(idx);

        String[] arr = response.split(";");
        if (count != arr.length) {
            Log.e("ThermControllerData", "Invalid number of sensors returned");
            return false;
        }

        for (int i = 0; i < count; i++) {
            int id = Integer.parseInt(arr[i].substring(0, 2), 16);

            RoomSensorData roomSensorData = roomSensorsMap.get(id);
            roomSensorData.decodeOrderAndName(arr[i]);
        }

        return true;
    }

    private boolean decodeBoilerSettings(String response) {
        if (response == null)
            return false;

//        int idx = 0;
//        for (int i = 0; i < BOILER_SENSOR_COUNT; i++)
//            idx = boilerSensors(i).decodeSettings(response, idx);
//
//        if (response.charAt(idx++) != '*') {
//            Log.e("ThermControllerData", "Not '*'");
//            return false;
//        }
//
//        for (int i = 0; i < BOILER_PUMP_COUNT; i++)
//            idx = boilerPumps(i).decodeSettings(response, idx);
        return true;
    }


    int decode(String response) {
        if (response == null) return Utils.FLAG_HAVE_NOTHING;

        Log.d("decode thermostat", response);

        String relaySettings = null;
        String sensorSettings = null;
        String boilerSettings = null;
        JSONObject jState = null;

        JSONObject jMain = null;
        try {
            jMain = new JSONObject(response);

            if (jMain.has("state"))
                jState = jMain.getJSONObject("state");

            JSONObject jSettings = null;
            if (jMain.has("settings"))
                jSettings = jMain.getJSONObject("settings");

            if (jSettings != null) {
                relaySettings = jSettings.getString("relays");
                sensorSettings = jSettings.getString("sensors");
                boilerSettings = jSettings.getString("boiler");
            }
        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
            return Utils.FLAG_HAVE_NOTHING;
        }

        int result = Utils.FLAG_HAVE_NOTHING;

        if (decodeRelaySettings(relaySettings) && decodeRoomSensorSettings(sensorSettings) && decodeBoilerSettings(boilerSettings)) {
            setHaveSettings(true);
            result = Utils.FLAG_HAVE_SETTINGS;
        }

        if (jState != null)
            result = result | decodeState(jState);

        return result;
    }

    private int decodeState(JSONObject jState) {
        if (!haveSettings())
            return Utils.FLAG_HAVE_NOTHING;

        try {
            // Relays
            String response = jState.getString("relays");
            setIsActive(response.charAt(0) != 'F');
            for (int i = 0; i < RELAY_COUNT; i++)
                relays(i).setIsOn(response.charAt(i + 1) == '1');

            // room sensors
            response = jState.getString("sensors");
            short count = Short.parseShort(response.substring(0, 2), 16);
            int idx = 2;

            for (int i = 0; i < count; i++) {
                Integer id = Integer.parseInt(response.substring(idx, idx + 2), 16);

                RoomSensorData roomSensorData = roomSensorsMap.get(id);
                if (roomSensorData != null)
                    idx = roomSensorData.decodeState(response, idx);
                else
                    idx += 11;
            }

            // boiler sensors
            response = jState.getString("boiler");
            idx = 0;
            for (int i = 0; i < BOILER_SENSOR_COUNT; i++) {
                idx = boilerSensorsData[i].decodeState(response, idx);
            }

            // boiler relays
            idx = 0;
            for (int i = 0; i < BOILER_PUMP_COUNT; i++)
                boilerPumpsData[i].setIsOn(response.charAt(idx + i) == '1');
            idx += BOILER_PUMP_COUNT;


            response = jState.getString("time");
            setNow(response.substring(0, 12));
        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
        }
        return Utils.FLAG_HAVE_STATE;
    }

    void decode(SharedPreferences prefs) {

        setIsActive(prefs.getBoolean("t_automatic_mode", false));

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).decodeSettings(prefs);

        for (int id : roomSensorsMap.keySet())
            roomSensors(id).decodeSettings(prefs);

        for (BoilerSensorData bs : boilerSensorsData)
            bs.decodeSettings(prefs);
    }

    void saveToPreferences(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("t_automatic_mode", isActive());

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).encodeSettings(editor);

        for (RoomSensorData ss : roomSensorsMap.values())
            ss.encodeSettings(editor);

        for (BoilerSensorData bs : boilerSensorsData)
            bs.encodeSettings(editor);

        editor.apply();
    }

    String GetStatusText() {
        return DateFormat.getDateTimeInstance().format(this.getNow());
    }
}
