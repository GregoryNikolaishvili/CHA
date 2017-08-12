package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.other.OtherControllerData;
import ge.altasoft.gia.cha.other.Sensor5in1Data;
import ge.altasoft.gia.cha.other.WaterLevelData;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;


public class MqttClientLocal {

    private static final String TOPIC_CHA_NOTIFICATION = "$SYS/broker/connection/cha_wrt_remote/state";

    private static final String TOPIC_CHA_SYS = "cha/sys/";
    private static final String TOPIC_CHA_ALERT = "cha/alert";
    private static final String TOPIC_CHA_LOG = "cha/log/"; // last "/" is important

    private static final String TOPIC_CHA_5IN1 = "cha/5in1_sensor/"; // last "/" is important
    private static final String TOPIC_CHA_ROOM_SENSOR_STATE = "cha/room_sensor/"; // last "/" is important
    //private static final String TOPIC_CHA_ROOM_SENSOR_STATE_REFREREFRESH = "cha/ROOM_SENSOR/"; // last "/" is important

    // Light controller
    private static final String TOPIC_CHA_LIGHT_CONTROLLER_STATE = "cha/lc/state";

    private static final String TOPIC_CHA_LIGHT_RELAY_STATE = "cha/lc/rs/"; // last "/" is important
    //private static final String TOPIC_CHA_LIGHT_RELAY_STATE_REFRESH = "cha/lc/Rs/"; // last "/" is important
    private static final String TOPIC_CHA_LIGHT_ALL_STATE_REFRESH = "cha/lc/refresh";
    private static final String TOPIC_CHA_LIGHTS_SETTINGS = "cha/lc/settings";
    private static final String TOPIC_CHA_LIGHTS_NAMES_AND_ORDER = "cha/lc/names";

    // ts. Thermostat controller
    private static final String TOPIC_CHA_THERMOSTAT_CONTROLLER_STATE = "cha/ts/state";

    private static final String TOPIC_CHA_THERMOSTAT_BOILER_SENSOR_STATE = "cha/ts/bs/"; // last "/" is important
    private static final String TOPIC_CHA_THERMOSTAT_BOILER_SENSOR_STATE_REFRESH = "cha/ts/Bs/"; // last "/" is important
    private static final String TOPIC_CHA_THERMOSTAT_BOILER_RELAY_STATE = "cha/ts/br/"; // last "/" is important
    private static final String TOPIC_CHA_THERMOSTAT_BOILER_RELAY_STATE_REFRESH = "cha/ts/Br/"; // last "/" is important
    private static final String TOPIC_CHA_THERMOSTAT_HEATER_RELAY_STATE = "cha/ts/hr/"; // last "/" is important
    private static final String TOPIC_CHA_THERMOSTAT_HEATER_RELAY_STATE_REFRESH = "cha/ts/Hr/"; // last "/" is important

    private static final String TOPIC_CHA_THERMOSTAT_BOILER_SETTINGS = "cha/ts/settings/bl";
    private static final String TOPIC_CHA_THERMOSTAT_ROOM_SENSOR_SETTINGS = "cha/ts/settings/rs";
    private static final String TOPIC_CHA_THERMOSTAT_ROOM_SENSOR_NAMES_AND_ORDER = "cha/ts/names/rs";

    // wl. Water level controller
    private static final String TOPIC_CHA_WATER_LEVEL_CONTROLLER_STATE = "cha/wl/state";

    private static final String TOPIC_CHA_WATER_LEVEL_SENSOR_STATE = "cha/wl/state/"; // last "/" is important
    private static final String TOPIC_CHA_WATER_LEVEL_SENSOR_STATE_REFRESH = "cha/wl/State/"; // last "/" is important

    private static final String TOPIC_CHA_WATER_LEVEL_SETTINGS = "cha/wl/settings";

    static final String MQTT_DATA_TYPE = "ge.altasoft.gia.cha.DATA_TYPE";

    public enum MQTTReceivedDataType {
        WrtState,
        Alert,
        Log,
        ClientConnected,

        Sensor5in1StateTH,
        Sensor5in1StateW,
        SensorRoomState,

        LightControllerState,
        LightRelayState,
        LightRelayStateRefresh,
        LightSettings,
        LightNameAndOrders,

        ThermostatControllerState,
        ThermostatBoilerSensorState,
        ThermostatBoilerPumpState,
        ThermostatRoomSensorSettings,
        ThermostatRoomSensorNameAndOrders,
        ThermostatHeaterRelayState,
        ThermostatBoilerSettings,

        WaterLevelControllerState,
        WaterLevelState,
        WaterLevelSettings
    }

    enum MQTTConnectionStatus {
        ERROR,
        INITIAL,                            // initial status
        CONNECTING,                         // attempting to connect
        CONNECTED,                          // connected
        NOTCONNECTED_USERDISCONNECT,        // user has explicitly requested
        NOTCONNECTED_UNKNOWNREASON          // failed to connect for some reason
    }

    // constants used to tell the Activity UI the connection status
    static final String MQTT_STATUS_INTENT = "ge.altasoft.gia.cha.STATUS";
    static final String MQTT_DATA_INTENT = "ge.altasoft.gia.cha.DATA";
    static final String MQTT_CONN_STATUS = "ge.altasoft.gia.cha.MSG.STATUS";
    static final String MQTT_MSG = "ge.altasoft.gia.cha.MSG";
    static final String MQTT_MSG_IS_ERROR = "ge.altasoft.gia.cha.MSG.IS_ERROR";

    final private Context context;

    private MqttAndroidClient mqttClient = null;

    private final String clientId;
    private String brokerUrl;

    private final ArrayList<String> connectedClients = new ArrayList<>();

    private MQTTConnectionStatus connectionStatus = MQTTConnectionStatus.INITIAL;

    MqttClientLocal(Context context) {
        this.context = context;
        //clientId = Utils.getDeviceName().concat("-").concat(Utils.getDeviceUniqueId(context));
        clientId = Utils.getDeviceName(); //
    }

    ArrayList<String> getConnectedClientList() {
        return connectedClients;
    }

    void start() {
        if (mqttClient != null)
            stop();

        Utils.readUrlSettings(context);
        brokerUrl = "tcp://" + Utils.getMtqqBrokerUrl(context);

        mqttClient = new MqttAndroidClient(context, brokerUrl, "android." + String.valueOf(System.currentTimeMillis()));
        //mqttClient.registerResources(context);
        mqttClient.setCallback(new MqttCallbackHandler());

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectToBroker();
            }
        }, "MQTTservice_start").start();
    }

    void stop() {
        if (mqttClient == null)
            return;

        broadcastServiceStatus("Disconnecting", false);

        publish(TOPIC_CHA_SYS.concat(clientId), "", true);
        try {
            //mqttClient.unsubscribe("cha/#");
            mqttClient.unregisterResources();
            mqttClient.close();
//                IMqttToken disconToken = getMqttClient.disconnect();
//                disconToken.setActionCallback(new IMqttActionListener() {
//                    @Override
//                    public void onSuccess(IMqttToken asyncActionToken) {
//                        Log.d("mqtt", "disconnect.onSuccess");
//                        connectionStatus = MQTTConnectionStatus.NOTCONNECTED_USERDISCONNECT;
//                        broadcastServiceStatus("Disconnected", false);
//                    }
//
//                    @Override
//                    public void onFailure(IMqttToken asyncActionToken,
//                                          Throwable exception) {
//                        // something went wrong, but probably we are disconnected anyway
//                        connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;
//                        broadcastServiceStatus("Disconnected", false);
//                    }
//                });
//        } catch (MqttPersistenceException e) {
//            Log.e("mqtt", "disconnect failed - persistence exception", e);
//            broadcastServiceStatus("disconnect failed - persistence exception: " + e.getMessage(), true);
//        } catch (MqttException e) {
//            Log.e("mqtt", "disconnect failed - MQTT exception", e);
//            broadcastServiceStatus("disconnect failed - MQTT exception: " + e.getMessage(), true);
        } finally {
            mqttClient = null;
        }
    }

    public void publish(String topic, String message, boolean retained) {
        if (mqttClient == null)
            return;

        Log.d("mqtt", String.format("publish. topic='%s', payload='%s'", topic, message));

        try {
            byte[] payload;
            if (message.equals(""))
                payload = new byte[0];
            else
                payload = message.getBytes("UTF-8");
            mqttClient.publish(topic, payload, 1, retained);
        } catch (UnsupportedEncodingException e) {
            Log.e("mqtt", "publish failed - UnsupportedEncodingException", e);
            broadcastServiceStatus("publish failed - UnsupportedEncodingException: " + e.getMessage(), true);
        } catch (MqttException e) {
            Log.e("mqtt", "publish failed - MQTT exception", e);
            broadcastServiceStatus("publish failed - MQTT exception: " + e.getMessage(), true);
        }
    }

    private void broadcastServiceStatus(String statusDescription, boolean isError) {
        Log.i("mqtt", statusDescription);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MQTT_STATUS_INTENT);
        broadcastIntent.putExtra(MQTT_MSG, statusDescription);
        broadcastIntent.putExtra(MQTT_MSG_IS_ERROR, isError);
        broadcastIntent.putExtra(MQTT_CONN_STATUS, connectionStatus);

        context.sendBroadcast(broadcastIntent);
    }

    private synchronized void connectToBroker() {
        Log.d("mqtt", "connecting");
        connectionStatus = MQTTConnectionStatus.CONNECTING;
        broadcastServiceStatus("Connecting...", false);

        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setWill(TOPIC_CHA_SYS.concat(clientId), new byte[0], 1, true);

            IMqttToken token = mqttClient.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("mqtt", "connect.onSuccess");
                    if (connectionStatus == MQTTConnectionStatus.CONNECTING) //todo workaround for strange bug. onsuccess was called twice
                    {
                        connectionStatus = MQTTConnectionStatus.CONNECTED;
                        broadcastServiceStatus("Connected", false);

                        publish(TOPIC_CHA_SYS.concat(clientId), "connected", true);
                        publish("chac/ts/refresh", "1", false);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                subscribeToTopics();
                            }
                        }, "MQTTservice_subscribe").start();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    connectionStatus = MQTTConnectionStatus.NOTCONNECTED_UNKNOWNREASON;
                    Log.e("mqtt", "connect failed", exception);
                    broadcastServiceStatus("Connect failed: " + exception.getMessage(), true);
                    connectToBroker();
                }
            });
        } catch (IllegalArgumentException e) {
            Log.e("mqtt", "connect failed - illegal argument", e);
            broadcastServiceStatus("connect failed - illegal argument: " + e.getMessage(), true);
        } catch (MqttException e) {
            Log.e("mqtt", "connect failed - MQTT exception", e);
            broadcastServiceStatus("connect failed - MQTT exception: " + e.getMessage(), true);
        }
    }

    private synchronized void subscribeToTopics() {
        broadcastServiceStatus("Subscribing...", false);

        try {
            IMqttToken subToken = mqttClient.subscribe("cha/#", 1);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("mqtt", "subscribe.onSuccess (cha/#)");
                    broadcastServiceStatus("connect.subscribed", false);
                    broadcastServiceStatus(brokerUrl, false);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // The subscription could not be performed, maybe the user was not authorized to subscribe on the specified topic e.g. using wildcards
                    broadcastServiceStatus("subscribe failed: " + exception.getMessage(), true);
                }
            });

            subToken = mqttClient.subscribe("$SYS/broker/connection/cha_wrt_remote/state", 1);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("mqtt", "subscribe.onSuccess (sys)");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // The subscription could not be performed, maybe the user was not authorized to subscribe on the specified topic e.g. using wildcards
                }
            });


        } catch (IllegalArgumentException e) {
            Log.e("mqtt", "subscribe failed - illegal argument", e);
            broadcastServiceStatus("subscribe failed - illegal argument: " + e.getMessage(), true);
        } catch (MqttException e) {
            Log.e("mqtt", "subscribe failed - MQTT exception", e);
            broadcastServiceStatus("subscribe failed - MQTT argument: " + e.getMessage(), true);
        }
    }


    //Checks if the MQTT client thinks it has an active connection
//    private boolean isAlreadyConnected() {
//        return (getMqttClient != null) && getMqttClient.isConnected();
//    }

//    private boolean isOnline() {
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
//        return cm.getActiveNetworkInfo() != null &&
//                cm.getActiveNetworkInfo().isAvailable() &&
//                cm.getActiveNetworkInfo().isConnected();
//    }

    private class MqttCallbackHandler implements MqttCallbackExtended {

        MqttCallbackHandler() {
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Log.d("mqtt", "connect complete: " + serverURI);
        }

        @Override
        public void connectionLost(Throwable cause) {
            if (cause != null) {
                Log.d("mqtt", "connection lost: " + cause.getMessage());
                broadcastServiceStatus("connection lost: " + cause.getMessage(), false);
            }

            if (mqttClient != null) {
                //mqttClient.unregisterResources();
                mqttClient = null;
            }

            if (cause != null)
                start();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String payload = message.toString();

            //Log.i("mqtt", String.format("message arrived. topic='%s', payload='%s'", topic, payload));

            Intent broadcastDataIntent = new Intent();
            broadcastDataIntent.setAction(MQTT_DATA_INTENT);
            try {
                switch (topic) {

                    case TOPIC_CHA_NOTIFICATION:
                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.WrtState);
                        broadcastDataIntent.putExtra("value", payload.equals("1"));
                        context.sendBroadcast(broadcastDataIntent);
                        return;

                    case TOPIC_CHA_ALERT:
                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.Alert);
                        broadcastDataIntent.putExtra("message", payload);
                        context.sendBroadcast(broadcastDataIntent);
                        return;

                    case TOPIC_CHA_LIGHT_CONTROLLER_STATE:
                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightControllerState);
                        broadcastDataIntent.putExtra("state", Integer.parseInt(payload, 16));
                        context.sendBroadcast(broadcastDataIntent);
                        return;

                    case TOPIC_CHA_LIGHT_ALL_STATE_REFRESH:
                        LightControllerData.Instance.decodeAllStates(payload);

                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightRelayStateRefresh);
                        context.sendBroadcast(broadcastDataIntent);
                        return;

                    case TOPIC_CHA_THERMOSTAT_CONTROLLER_STATE:
                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.ThermostatControllerState);
                        broadcastDataIntent.putExtra("state", Integer.parseInt(payload, 16));
                        context.sendBroadcast(broadcastDataIntent);
                        return;

                    case TOPIC_CHA_WATER_LEVEL_CONTROLLER_STATE:
                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.WaterLevelControllerState);
                        broadcastDataIntent.putExtra("state", Integer.parseInt(payload, 16));
                        context.sendBroadcast(broadcastDataIntent);
                        return;


                    //region Lights
                    case TOPIC_CHA_LIGHTS_SETTINGS:
                        LightControllerData.Instance.decodeSettings(payload);

                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightSettings);
                        context.sendBroadcast(broadcastDataIntent);
                        return;

                    case TOPIC_CHA_LIGHTS_NAMES_AND_ORDER:
                        LightControllerData.Instance.decodeNamesAndOrder(payload);

                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightNameAndOrders);
                        context.sendBroadcast(broadcastDataIntent);
                        return;
                    //endregion

                    //region Thermostat
                    case TOPIC_CHA_THERMOSTAT_ROOM_SENSOR_SETTINGS:
                        ThermostatControllerData.Instance.decodeRoomSensorSettings(payload);

                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.ThermostatRoomSensorSettings);
                        context.sendBroadcast(broadcastDataIntent);
                        return;

                    case TOPIC_CHA_THERMOSTAT_ROOM_SENSOR_NAMES_AND_ORDER:
                        ThermostatControllerData.Instance.decodeRoomSensorNamesAndOrder(payload);

                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.ThermostatRoomSensorNameAndOrders);
                        context.sendBroadcast(broadcastDataIntent);
                        return;

                    case TOPIC_CHA_THERMOSTAT_BOILER_SETTINGS:
                        ThermostatControllerData.Instance.decodeBoilerSettings(payload);

                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.ThermostatBoilerSettings);
                        context.sendBroadcast(broadcastDataIntent);
                        return;
                    //endregion

                    //region Water level
                    case TOPIC_CHA_WATER_LEVEL_SETTINGS:
                        OtherControllerData.Instance.decodeWaterLevelSettings(payload);

                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.WaterLevelSettings);
                        context.sendBroadcast(broadcastDataIntent);
                        return;
                    //endregion
                }

                if (topic.startsWith(TOPIC_CHA_LIGHT_RELAY_STATE)) {
                    int id = Integer.parseInt(topic.substring(TOPIC_CHA_LIGHT_RELAY_STATE.length()), 16);
                    LightControllerData.Instance.relays(id).decodeState(payload);

                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightRelayState);
                    broadcastDataIntent.putExtra("id", id);
                    context.sendBroadcast(broadcastDataIntent);

                    return;
                }

                if (topic.startsWith(TOPIC_CHA_ROOM_SENSOR_STATE)) {
                    int id = Integer.parseInt(topic.substring(TOPIC_CHA_ROOM_SENSOR_STATE.length()), 16);
                    RoomSensorData rs = ThermostatControllerData.Instance.roomSensors(id, false);
                    if (rs == null)
                        broadcastDataIntent.putExtra("new_sensor", true);
                    ThermostatControllerData.Instance.roomSensors(id, true).decodeState(payload);

                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.SensorRoomState);
                    broadcastDataIntent.putExtra("id", id);
                    context.sendBroadcast(broadcastDataIntent);
                    return;
                }

                if (topic.startsWith(TOPIC_CHA_5IN1)) {
                    int id = Integer.parseInt(topic.substring(TOPIC_CHA_5IN1.length()), 16);
                    boolean isWeatherSensor = id == 0xA0000;
                    if ((id == 0) || isWeatherSensor) {
                        Sensor5in1Data sd = OtherControllerData.Instance.get5in1SensorData();
                        sd.decodeState(payload, isWeatherSensor);

                        broadcastDataIntent.putExtra(MQTT_DATA_TYPE, isWeatherSensor ? MQTTReceivedDataType.Sensor5in1StateW : MQTTReceivedDataType.Sensor5in1StateTH);
                        broadcastDataIntent.putExtra("id", 0);
                        context.sendBroadcast(broadcastDataIntent);
                    }
                    return;
                }

                if (topic.startsWith(TOPIC_CHA_WATER_LEVEL_SENSOR_STATE) || topic.startsWith(TOPIC_CHA_WATER_LEVEL_SENSOR_STATE_REFRESH)) {
                    int id = Integer.parseInt(topic.substring(TOPIC_CHA_WATER_LEVEL_SENSOR_STATE.length()), 16);
                    WaterLevelData wl = OtherControllerData.Instance.getWaterLevelData(id);
                    if (wl != null)
                        wl.decodeState(payload);

                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.WaterLevelState);
                    broadcastDataIntent.putExtra("id", id);
                    context.sendBroadcast(broadcastDataIntent);
                    return;
                }

                if (topic.startsWith(TOPIC_CHA_THERMOSTAT_HEATER_RELAY_STATE) || topic.startsWith(TOPIC_CHA_THERMOSTAT_HEATER_RELAY_STATE_REFRESH)) {
                    int id = Integer.parseInt(topic.substring(TOPIC_CHA_THERMOSTAT_HEATER_RELAY_STATE.length()), 16);

                    ArrayList<Integer> ids = ThermostatControllerData.Instance.setHeaterRelayIsOn(id, !payload.equals("0"));
                    for (int idx : ids) {
                        Intent intent = new Intent();
                        intent.setAction(MQTT_DATA_INTENT);
                        intent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.SensorRoomState);
                        intent.putExtra("id", idx);
                        context.sendBroadcast(intent);
                    }

                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.ThermostatHeaterRelayState);
                    broadcastDataIntent.putExtra("id", id);
                    context.sendBroadcast(broadcastDataIntent);

                    return;
                }

                if (topic.startsWith(TOPIC_CHA_THERMOSTAT_BOILER_SENSOR_STATE) || topic.startsWith(TOPIC_CHA_THERMOSTAT_BOILER_SENSOR_STATE_REFRESH)) {
                    int id = Integer.parseInt(topic.substring(TOPIC_CHA_THERMOSTAT_BOILER_SENSOR_STATE.length()), 16);
                    ThermostatControllerData.Instance.boilerSensors(id).decodeState(payload);

                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.ThermostatBoilerSensorState);
                    broadcastDataIntent.putExtra("id", id);
                    context.sendBroadcast(broadcastDataIntent);
                    return;
                }

                if (topic.startsWith(TOPIC_CHA_THERMOSTAT_BOILER_RELAY_STATE) || topic.startsWith(TOPIC_CHA_THERMOSTAT_BOILER_RELAY_STATE_REFRESH)) {
                    int id;
                    try {
                        id = Integer.parseInt(topic.substring(TOPIC_CHA_THERMOSTAT_BOILER_RELAY_STATE.length()), 16);
                    } catch (NumberFormatException ex) {
                        return;
                    }

                    ThermostatControllerData.Instance.boilerPumps(id).decodeState(payload);

                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.ThermostatBoilerPumpState);
                    broadcastDataIntent.putExtra("id", id);
                    context.sendBroadcast(broadcastDataIntent);

                    return;
                }

                ////////////////////

                if (topic.startsWith(TOPIC_CHA_LOG)) {
                    String type = topic.substring(TOPIC_CHA_LOG.length());

                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.Log);
                    broadcastDataIntent.putExtra("type", type);
                    broadcastDataIntent.putExtra("log", payload);
                    context.sendBroadcast(broadcastDataIntent);
                }

                if (topic.startsWith(TOPIC_CHA_SYS)) {
                    String clientId = topic.substring(TOPIC_CHA_SYS.length());
                    switch (payload) {
                        case "connected":
                            if (!connectedClients.contains(clientId))
                                connectedClients.add(clientId);

                            broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.ClientConnected);
                            broadcastDataIntent.putExtra("id", clientId);
                            broadcastDataIntent.putExtra("value", true);
                            context.sendBroadcast(broadcastDataIntent);
                            break;

                        case "disconnected":
                        case "":
                            if (connectedClients.contains(clientId))
                                connectedClients.remove(clientId);

                            broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.ClientConnected);
                            broadcastDataIntent.putExtra("id", clientId);
                            broadcastDataIntent.putExtra("value", false);
                            context.sendBroadcast(broadcastDataIntent);
                            break;
                    }
                }
            } catch (Exception ex) {
                Log.e("mqtt", ex.getMessage(), ex);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // Do nothing
        }
    }
}
