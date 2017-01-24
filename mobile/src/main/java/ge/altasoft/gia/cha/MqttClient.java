package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import ge.altasoft.gia.cha.light.LightControllerData;


public class MqttClient {

    private static final String TOPIC_CHA_SYS_OLD = "cha/sys";

    private static final String TOPIC_CHA_SYS = "cha/sys/";
    private static final String TOPIC_CHA_LIGHTS_STATE = "cha/light/state/";

    private static final String TOPIC_CHA_LIGHTS_SYS = "cha/light/sys";
    private static final String TOPIC_CHA_LIGHTS_SETTINGS = "cha/light/settings";
    private static final String TOPIC_CHA_LIGHTS_NAMES_AND_ORDER = "cha/light/names";

    private static final String TOPIC_CHA_THERMOSTAT_FULL = "cha/thermostat/full";

    public static final String MQTT_DATA_TYPE = "ge.altasoft.gia.cha.DATA_TYPE";

    public static enum MQTTReceivedDataType {
        HaveNothing,
        LightControllerConnected,
        LightOneState,
        LightAllStates,
        LightSettings,
        LightNameAndOrders,
        ThermostatOneState
    }

    public enum MQTTConnectionStatus {
        INITIAL,                            // initial status
        CONNECTING,                         // attempting to connect
        CONNECTED,                          // connected
        NOTCONNECTED_USERDISCONNECT,        // user has explicitly requested
        NOTCONNECTED_UNKNOWNREASON          // failed to connect for some reason
    }

    // constants used to tell the Activity UI the connection status
    public static final String MQTT_STATUS_INTENT = "ge.altasoft.gia.cha.STATUS";
    public static final String MQTT_DATA_INTENT = "ge.altasoft.gia.cha.DATA";
    public static final String MQTT_CONN_STATUS = "ge.altasoft.gia.cha.MSG.STATUS";
    public static final String MQTT_MSG = "ge.altasoft.gia.cha.MSG";
    public static final String MQTT_MSG_IS_ERROR = "ge.altasoft.gia.cha.MSG.IS_ERROR";

    final private Context context;

    private MqttAndroidClient mqttClient = null;

    private String clientId;
    private String brokerUrl;

    private ArrayList<String> connectedClients = new ArrayList<>();

    private MQTTConnectionStatus connectionStatus = MQTTConnectionStatus.INITIAL;

    MqttClient(Context context) {
        this.context = context;
        clientId = Utils.getDeviceName().concat("-").concat(Utils.getDeviceUniqueId(context));
    }

    public ArrayList<String> getConnectedClientList() {
        return connectedClients;
    }

    void start() {
        if (mqttClient != null)
            stop();

        Utils.readUrlSettings(context);
        brokerUrl = "tcp://" + Utils.getMtqqBrokerUrl(context);

        mqttClient = new MqttAndroidClient(context, brokerUrl, "acha." + String.valueOf(System.currentTimeMillis()));
        mqttClient.registerResources(context);
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
            mqttClient.unsubscribe("cha/#");
            mqttClient.disconnect();
            mqttClient.unregisterResources();
//                IMqttToken disconToken = mqttClient.disconnect();
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
        } catch (MqttPersistenceException e) {
            Log.e("mqtt", "disconnect failed - persistence exception", e);
            broadcastServiceStatus("disconnect failed - persistence exception: " + e.getMessage(), true);
        } catch (MqttException e) {
            Log.e("mqtt", "disconnect failed - MQTT exception", e);
            broadcastServiceStatus("disconnect failed - MQTT exception: " + e.getMessage(), true);
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
            Log.e("mqtt", "subscribe failed - illegal argument", e);
            broadcastServiceStatus("subscribe failed - illegal argument: " + e.getMessage(), true);
        } catch (MqttException e) {
            Log.e("mqtt", "subscribe failed - MQTT exception", e);
            broadcastServiceStatus("subscribe failed - MQTT exception: " + e.getMessage(), true);
        }
    }

    private synchronized void subscribeToTopics() {
        broadcastServiceStatus("Subscribing...", false);

        try {
            IMqttToken subToken = mqttClient.subscribe("cha/#", 1);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("mqtt", "subscribe.onSuccess");
                    broadcastServiceStatus("connect.subscribed", false);
                    broadcastServiceStatus(brokerUrl, false);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // The subscription could not be performed, maybe the user was not authorized to subscribe on the specified topic e.g. using wildcards
                    broadcastServiceStatus("subscribe failed: " + exception.getMessage(), true);
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
//        return (mqttClient != null) && mqttClient.isConnected();
//    }

//    private boolean isOnline() {
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
//        return cm.getActiveNetworkInfo() != null &&
//                cm.getActiveNetworkInfo().isAvailable() &&
//                cm.getActiveNetworkInfo().isConnected();
//    }

    private class MqttCallbackHandler implements MqttCallback {

        MqttCallbackHandler() {
        }

        @Override
        public void connectionLost(Throwable cause) {
            if (cause != null) {
                Log.d("mqtt", "connection lost: " + cause.getMessage());
                broadcastServiceStatus("connection lost: " + cause.getMessage(), false);
            }

            start();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String payload = message.toString();

            Log.i("mqtt", String.format("message arrived. topic='%s', payload='%s'", topic, payload));

            Intent broadcastDataIntent = new Intent();
            broadcastDataIntent.setAction(MQTT_DATA_INTENT);

            switch (topic) {
                case TOPIC_CHA_SYS_OLD: // TODO: 1/24/2017  obsolete
                    switch (payload) {
                        case "light controller connected":
                            broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightControllerConnected);
                            broadcastDataIntent.putExtra("value", true);
                            context.sendBroadcast(broadcastDataIntent);
                            break;

                        case "light controller disconnected":
                            broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightControllerConnected);
                            broadcastDataIntent.putExtra("value", false);
                            context.sendBroadcast(broadcastDataIntent);
                            break;
                    }
                    break;

                // TODO: 1/25/2017
                // აქ აღარ მინდა ეს. ქვემოთ იყოს
                case TOPIC_CHA_LIGHTS_SYS:
                    switch (payload) {
                        case "connected":
                            broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightControllerConnected);
                            broadcastDataIntent.putExtra("value", true);
                            context.sendBroadcast(broadcastDataIntent);
                            break;

                        case "disconnected":
                            broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightControllerConnected);
                            broadcastDataIntent.putExtra("value", false);
                            context.sendBroadcast(broadcastDataIntent);
                            break;
                    }
                    break;

                case TOPIC_CHA_LIGHTS_SETTINGS:
                    LightControllerData.Instance.decodeSettings(payload);

                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightSettings);
                    context.sendBroadcast(broadcastDataIntent);
                    break;

                case TOPIC_CHA_LIGHTS_NAMES_AND_ORDER:
                    LightControllerData.Instance.decodeNamesAndOrder(payload);

                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightNameAndOrders);
                    context.sendBroadcast(broadcastDataIntent);
                    break;

//                case TOPIC_CHA_THERMOSTAT_FULL:
//                    ThermostatControllerData.Instance.decode(payload);
//
//                    broadcastDataIntent.putExtra(MQTT_DATA_TYPE, Utils.FLAG_HAVE_THERMOSTAT_FULL_STATE);
//                    context.sendBroadcast(broadcastDataIntent);
//                    break;
            }

            if (topic.startsWith(TOPIC_CHA_LIGHTS_STATE)) {
                int id = Integer.parseInt(topic.substring(TOPIC_CHA_LIGHTS_STATE.length()), 16);
                boolean value = !payload.equals("0");
                LightControllerData.Instance.relays(id - 1).setIsOn(value);

                broadcastDataIntent.putExtra(MQTT_DATA_TYPE, MQTTReceivedDataType.LightOneState);
                broadcastDataIntent.putExtra("id", id);
                broadcastDataIntent.putExtra("value", value);
                context.sendBroadcast(broadcastDataIntent);

                return;
            }

            // TODO: 1/25/2017
            // აქ დავამატო შუქის და თერმოსტატის კონტროლერიც
            if (topic.startsWith(TOPIC_CHA_SYS)) {
                String clientId = topic.substring(TOPIC_CHA_SYS.length());
                switch (payload) {
                    case "connected":
                        if (!connectedClients.contains(clientId))
                            connectedClients.add(clientId);
                        break;

                    case "disconnected":
                    case "":
                        if (connectedClients.contains(clientId))
                            connectedClients.remove(clientId);
                        break;
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // Do nothing
        }
    }
}
