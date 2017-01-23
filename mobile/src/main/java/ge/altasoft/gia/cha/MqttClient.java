package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
//import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.io.UnsupportedEncodingException;

import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

//import static android.content.Context.CONNECTIVITY_SERVICE;

public class MqttClient {

    public static final String TOPIC_CHA_SYS = "cha/sys";
    private static final String TOPIC_CHA_SYS_ME = "cha/sys/me";
    private static final String TOPIC_CHA_LIGHTS_STATE = "cha/light/state";
    private static final String TOPIC_CHA_LIGHTS_SETTINGS = "cha/light/settings";
    private static final String TOPIC_CHA_LIGHTS_NAMES_AND_ORDER = "cha/light/names";

    private static final String TOPIC_CHA_THERMOSTAT_FULL = "cha/thermostat/full";

    public enum MQTTConnectionStatus {
        INITIAL,                            // initial status
        CONNECTING,                         // attempting to connect
        CONNECTED_CHA_IS_ONLINE,           // connected, cha controller is online
        CONNECTED_CHA_IS_OFFLINE,           // connected, cha controller is offline
        //NOTCONNECTED_WAITINGFORINTERNET,    // can't connect because the phone
        //     does not have Internet access
        NOTCONNECTED_USERDISCONNECT,        // user has explicitly requested
        //     disconnection
        //NOTCONNECTED_DATADISABLED,          // can't connect because the user
        //     has disabled data access
        NOTCONNECTED_UNKNOWNREASON          // failed to connect for some reason
    }

    //public static MqttClient Instance = null;
    //private MqttAndroidClient mqttClient;

    private MQTTConnectionStatus connectionStatus = MQTTConnectionStatus.INITIAL;
    //private Context context;

    // constants used to tell the Activity UI the connection status
    public static final String MQTT_STATUS_INTENT = "ge.altasoft.gia.cha.STATUS";
    public static final String MQTT_DATA_INTENT = "ge.altasoft.gia.cha.DATA";
    public static final String MQTT_CONN_STATUS = "ge.altasoft.gia.cha.MSG.STATUS";
    public static final String MQTT_MSG = "ge.altasoft.gia.cha.MSG";
    public static final String MQTT_MSG_IS_ERROR = "ge.altasoft.gia.cha.MSG.IS_ERROR";

    final private Context context;
    private MqttAndroidClient mqttClient = null;
    private String url;

    MqttClient(Context context) {
        this.context = context;
    }

    void start() {
        if (mqttClient != null)
            stop();

        Utils.readUrlSettings(context);
        url = "tcp://" + Utils.getMtqqBrokerUrl(context);

        mqttClient = new MqttAndroidClient(context, url, "acha." + String.valueOf(System.currentTimeMillis()));
        mqttClient.registerResources(context);
        mqttClient.setCallback(new MqttCallbackHandler());

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectToBroker();
            }
        }, "MQTTservice").start();
    }

    void stop() {
        broadcastServiceStatus("Disconnecting", false);

        try {
            mqttClient.unregisterResources();
            mqttClient.disconnect();
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

    public void publish(String topic, String payload) {
        if (mqttClient == null)
            return;

        Log.d("mqtt", String.format("publish. topic='%s', payload='%s'", topic, payload));

        try {
            byte[] encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(false);
            mqttClient.publish(topic, message);
        } catch (UnsupportedEncodingException e) {
            Log.e("mqtt", "publish failed - UnsupportedEncodingException", e);
            broadcastServiceStatus("publish failed - MQTT exception: " + e.getMessage(), true);
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
            IMqttToken token = mqttClient.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("mqtt", "connect.onSuccess");
                    if (connectionStatus == MQTTConnectionStatus.CONNECTING) //todo workaround for strange bug. onsuccess was called twice
                    {
                        connectionStatus = MQTTConnectionStatus.CONNECTED_CHA_IS_OFFLINE;
                        broadcastServiceStatus("Connected", false);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                subscribeToTopics();
                            }
                        }, "MQTTservice").start();
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
                    broadcastServiceStatus(url, false);
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

            if (topic.equals(TOPIC_CHA_SYS_ME)) {
                //Toast.makeText(context, payload, Toast.LENGTH_SHORT).show();
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MQTT_DATA_INTENT);
                broadcastIntent.putExtra("what", Utils.FLAG_HAVE_WHO_IS_ACTIVE);
                broadcastIntent.putExtra("value", payload);
                context.sendBroadcast(broadcastIntent);
            } else if (topic.equals(TOPIC_CHA_SYS)) {
                if (payload.equals("who")) {
                    publish(TOPIC_CHA_SYS_ME, Utils.getDeviceName());
                } else if ((connectionStatus == MQTTConnectionStatus.CONNECTED_CHA_IS_OFFLINE) && payload.equals("light controller connected")) {
                    connectionStatus = MQTTConnectionStatus.CONNECTED_CHA_IS_ONLINE;
                    broadcastServiceStatus(url, false);
                } else if ((connectionStatus == MQTTConnectionStatus.CONNECTED_CHA_IS_ONLINE) && payload.equals("light controller disconnected")) {
                    connectionStatus = MQTTConnectionStatus.CONNECTED_CHA_IS_OFFLINE;
                    broadcastServiceStatus(url, false);
                }
            } else if (topic.equals(TOPIC_CHA_LIGHTS_SETTINGS)) {
                LightControllerData.Instance.decodeSettings(payload);

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MQTT_DATA_INTENT);
                broadcastIntent.putExtra("what", Utils.FLAG_HAVE_SETTINGS);
                context.sendBroadcast(broadcastIntent);
            } else if (topic.equals(TOPIC_CHA_LIGHTS_NAMES_AND_ORDER)) {
                LightControllerData.Instance.decodeNamesAndOrder(payload);

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MQTT_DATA_INTENT);
                broadcastIntent.putExtra("what", Utils.FLAG_HAVE_NAME_AND_ORDER);
                context.sendBroadcast(broadcastIntent);
            } else if (topic.startsWith(TOPIC_CHA_LIGHTS_STATE)) {
                int id = Integer.parseInt(topic.substring(TOPIC_CHA_LIGHTS_STATE.length() + 1), 16);
                boolean value = !payload.equals("0");
                LightControllerData.Instance.relays(id - 1).setIsOn(value);

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MQTT_DATA_INTENT);
                broadcastIntent.putExtra("what", Utils.FLAG_HAVE_LIGHTS_ONE_STATE);
                broadcastIntent.putExtra("id", id);
                broadcastIntent.putExtra("value", value);
                context.sendBroadcast(broadcastIntent);
            } else if (topic.equals(TOPIC_CHA_THERMOSTAT_FULL)) {
                ThermostatControllerData.Instance.decode(payload);

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(MQTT_DATA_INTENT);
                broadcastIntent.putExtra("what", Utils.FLAG_HAVE_THERMOSTAT_FULL_STATE);
                context.sendBroadcast(broadcastIntent);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // Do nothing
        }
    }
}
