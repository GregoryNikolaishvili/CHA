package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import ge.altasoft.gia.cha.classes.RelayControllerData;
import ge.altasoft.gia.cha.Utils;

public final class ThermostatControllerData extends RelayControllerData {

    final static int BOILER_SENSOR_SOLAR_PANEL = 0;
    final static int BOILER_SENSOR_BOTTOM = 1;
    final static int BOILER_SENSOR_TOP = 2;
    final static int BOILER_SENSOR_ROOM = 3;

    final static int BOILER_SOLAR_PUMP = 0;
    final static int BOILER_HEATING_PUMP = 1;

    final private static int RELAY_COUNT = 15;
    final public static int BOILER_SENSOR_COUNT = 4;
    final private static int BOILER_PUMP_COUNT = 2;
    final private static int ROOM_SENSOR_MAX_COUNT = 30;

    final public static ThermostatControllerData Instance = new ThermostatControllerData();

    private BoilerSensorData[] boilerSensorsData;
    private BoilerPumpData[] boilerPumpsData;

    private HashMap<Integer, RoomSensorData> roomSensors;
    private int[] savedRoomSensorOrders;
    private boolean roomSensorsReordered;

    private ThermostatControllerData() {
        super();

        roomSensorsReordered = false;

        boilerSensorsData = new BoilerSensorData[BOILER_SENSOR_COUNT];
        boilerPumpsData = new BoilerPumpData[BOILER_PUMP_COUNT];

        roomSensors = new HashMap<Integer, RoomSensorData>();
        savedRoomSensorOrders = new int[ROOM_SENSOR_MAX_COUNT];

        for (int i = 0; i < BOILER_SENSOR_COUNT; i++)
            boilerSensorsData[i] = new BoilerSensorData(i + 1);

        for (int i = 0; i < BOILER_PUMP_COUNT; i++)
            boilerPumpsData[i] = new BoilerPumpData(i + 1);

        boilerPumpsData[BOILER_SOLAR_PUMP].setName("Solar pump");
        boilerPumpsData[BOILER_HEATING_PUMP].setName("Heating pump");
    }

    @Override
    public int relayCount() {
        return RELAY_COUNT;
    }

    public ThermostatRelayData relays(int index) {
        return (ThermostatRelayData) super.relays(index);
    }

    BoilerPumpData boilerPumps(int index) {
        return boilerPumpsData[index];
    }

    public BoilerSensorData boilerSensors(int index) {
        return boilerSensorsData[index];
    }


    Map<Integer, RoomSensorData> sortedRoomSensors() {
        return sortByOrder(roomSensors);
    }

    RoomSensorData roomSensor(int id) {
        return Instance.roomSensors.get(id);
    }

    public void saveRoomSensorOrders() {
        roomSensorsReordered = false;

        //TODO
//        for (int i = 0; i < ThermostatUtils.ROOM_SENSOR_MAX_COUNT; i++) {
//            savedRoomSensorOrders[i] = roomSensors.get(i).order;
//        }
    }

    public void restoreRoomSensorRelayOrders() {
        //TODO
//        for (int i = 0; i < ThermostatUtils.ROOM_SENSOR_MAX_COUNT; i++) {
//            roomSensors.get(i).order = savedRoomSensorOrders[i];
//        }
        roomSensorsReordered = false;
    }

    public boolean roomSensorOrderChanged() {
        return roomSensorsReordered;
    }

    private RoomSensorData getRoomSensorFromUIIndex(int index) {

        Map<Integer, RoomSensorData> ss = sortByOrder(roomSensors);

        int idx = 0;
        for (int id : ss.keySet()) {
            if (idx == index)
                return roomSensors.get(id);
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

    String encodeState() {
        StringBuilder sb = new StringBuilder();

        sb.append('$');
        sb.append(isActive() ? 'T' : 'F');

        if (haveSettings()) {
            // Relays
            for (int i = 0; i < RELAY_COUNT; i++)
                relays(i).encodeState(sb);

            // boiler sensors
            for (int i = 0; i < BOILER_SENSOR_COUNT; i++)
                boilerSensorsData[i].encodeState(sb);

            // boiler relays
            for (int i = 0; i < BOILER_PUMP_COUNT; i++)
                //boilerPumpsData[i].encodeState(sb);
                sb.append((new Random()).nextBoolean() ? '1' : '0');


            // Room sensors
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
                sb.append(String.format(Locale.US, "%02X", roomSensors.size()));
                for (int id : roomSensors.keySet()) {
                    roomSensors.get(id).encodeState(sb);
                }
            }
        } else {
            // Relays
            for (int i = 0; i < RELAY_COUNT; i++)
                sb.append('0');
            // boiler sensors
            for (int i = 0; i < BOILER_SENSOR_COUNT; i++)
                sb.append(String.format(Locale.US, "%04XN", (i + 1) * 100));
            // boiler relays
            for (int i = 0; i < BOILER_PUMP_COUNT; i++)
                sb.append('0');
            // Room sensors
            sb.append(String.format(Locale.US, "%02X", 0));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
        sb.append(sdf.format(getNow()));

        return sb.toString();
    }

    public String encodeSettings() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        sb.append('*');
        sb.append(isActive() ? 'T' : 'F');

        sb.append('*');
        for (int i = 0; i < RELAY_COUNT; i++) {
            if (haveSettings())
                relays(i).encodeSettings(sb);
            else ThermostatRelayData.encodeSettingsDebug(sb);
        }

        sb.append('*');
        for (int i = 0; i < RELAY_COUNT; i++) {
            if (haveSettings())
                relays(i).encodeOrderAndName(sb2);
            else
                ThermostatRelayData.encodeOrderAndNameDebug(sb2, i);
        }

        if (haveSettings()) {
            sb.append(String.format(Locale.US, "%02X", roomSensors.size()));
            for (int id : roomSensors.keySet())
                roomSensors.get(id).encodeOrderAndName(sb2);
        } else {
            if (Utils.DEBUG_THERMOSTAT) {
                sb.append(String.format(Locale.US, "%02X", 10));

                for (int id = 1; id <= 10; id++) {
                    sb2.append(String.format(Locale.US, "%02X%01X", id, id));
                    sb2.append(Utils.EncodeArduinoString("Sensor #" + id));
                    sb2.append(';');
                }
            } else
                sb.append(String.format(Locale.US, "%02X", 0));
        }
        sb2.insert(0, String.format(Locale.US, "%04X", sb2.length()));
        sb.append(sb2);

        return sb.toString();
    }

    public int decode(String response) {
        if (response == null) return Utils.FLAG_HAVE_NOTHING;

        Log.d("decode thermostat", response);

        if ((response.charAt(0) != '$') && (response.charAt(0) != '*')) {
            Log.e("ThermControllerData", "Not '$' or '*'");
            return Utils.FLAG_HAVE_NOTHING;
        }

        if (response.charAt(0) == '$') { // receive state
            if (!haveSettings())
                return Utils.FLAG_HAVE_NOTHING;

            setIsActive(response.charAt(1) != 'F');

            // Relays
            for (int i = 0; i < RELAY_COUNT; i++)
                relays(i).setIsOn(response.charAt(i + 2) == '1');

            // boiler sensors
            int idx = RELAY_COUNT + 2;
            for (int i = 0; i < BOILER_SENSOR_COUNT; i++) {
                idx = boilerSensorsData[i].decodeState(response, idx);
            }

            // boiler relays
            for (int i = 0; i < BOILER_PUMP_COUNT; i++)
                boilerPumpsData[i].setIsOn(response.charAt(idx + i) == '1');
            idx += BOILER_PUMP_COUNT;

            // room sensors
            short count = Short.parseShort(response.substring(idx, idx + 2), 16);
            idx += 2;

            for (int i = 0; i < count; i++) {
                Integer id = Integer.parseInt(response.substring(idx, idx + 2), 16);

                RoomSensorData roomSensorData = roomSensors.get(id);
                if (roomSensorData != null)
                    idx = roomSensorData.decodeState(response, idx);
            }

            setNow(response.substring(idx, idx + 12));

            return Utils.FLAG_HAVE_STATE;
        }

        setIsActive(response.charAt(1) != 'F');
        int idx = 2;

        if (response.charAt(idx) != '*') {
            Log.e("ThermControllerData", "Not '*'");
            return Utils.FLAG_HAVE_NOTHING;
        }
        idx++;

        for (int i = 0; i < RELAY_COUNT; i++) {
            ThermostatRelayData relay = new ThermostatRelayData(i + 1);
            idx = relay.decodeSettings(response, idx);
            setRelay(i, relay);
        }

        setHaveSettings(true);

        if (response.charAt(idx) != '*') {
            Log.e("ThermControllerData", "Not '*'");
            return Utils.FLAG_HAVE_NOTHING;
        }
        //int length = Integer.parseInt(response.substring(idx + 1, idx + 5), 16);

        response = response.substring(idx + 5);

        String[] arr = response.split("\\$"); // maybe there's also state data
        response = arr[0];
        String stateResponse = null;
        if (arr.length == 2)
            stateResponse = '$' + arr[1];

        arr = response.split(";");
        if (arr.length < RELAY_COUNT) {
            Log.e("ThermControllerData", "Invalid number of relays returned");
            return Utils.FLAG_HAVE_NOTHING;
        }

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).decodeOrderAndName(arr[i]);

        for (int i = RELAY_COUNT; i < arr.length; i++) {
            int id = Integer.parseInt(arr[i].substring(0, 2), 16);
            RoomSensorData roomSensorData = roomSensors.get(id);

            if (roomSensorData == null) {
                roomSensorData = new RoomSensorData(id);
                //drawUI.createNewSensor(roomSensorData);
                roomSensors.put(id, roomSensorData);
            }

            roomSensorData.decodeOrderAndName(arr[i]);
        }

        if (stateResponse != null)
            return Utils.FLAG_HAVE_SETTINGS | decode(stateResponse);

        return Utils.FLAG_HAVE_SETTINGS;
    }

    public static void saveToPreferences(SharedPreferences prefs) {
        ThermostatControllerData ss = ThermostatControllerData.Instance;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("t_automatic_mode", ss.isActive());

        for (int i = 0; i < RELAY_COUNT; i++) {
            String suffix = Integer.toString(i + 1);

            ThermostatRelayData rs = ss.relays(i);

            editor.putString("t_relay_name_" + suffix, rs.getName());
        }

        editor.apply();
    }

    String GetStatusText() {
        return DateFormat.getDateTimeInstance().format(this.getNow());
    }
}
