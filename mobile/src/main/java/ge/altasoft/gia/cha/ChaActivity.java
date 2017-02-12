package ge.altasoft.gia.cha;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public abstract class ChaActivity extends AppCompatActivity {

    private String lastStatusMessage = "";
    private String lastErrorMessage = "";
    private long lastErrorMessageTime = 0;

    final private BroadcastReceiver broadcastStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String statusMessage = intent.getStringExtra(MqttClientLocal.MQTT_MSG);
            boolean isError = intent.getBooleanExtra(MqttClientLocal.MQTT_MSG_IS_ERROR, false);

            MqttClientLocal.MQTTConnectionStatus status = (MqttClientLocal.MQTTConnectionStatus) intent.getSerializableExtra(MqttClientLocal.MQTT_CONN_STATUS);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                if (isError) {
                    status = MqttClientLocal.MQTTConnectionStatus.ERROR;
                    lastErrorMessage = statusMessage;
                    lastErrorMessageTime = System.currentTimeMillis();
                    actionBar.setSubtitle(lastStatusMessage.concat(", ").concat(statusMessage));
                } else {
                    lastStatusMessage = statusMessage;

                    if ((status == MqttClientLocal.MQTTConnectionStatus.CONNECTED) || ((System.currentTimeMillis() - lastErrorMessageTime) > 120000))
                        actionBar.setSubtitle(statusMessage);
                    else
                        actionBar.setSubtitle(statusMessage.concat(", ").concat(lastErrorMessage));
                }
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                if (toolbar != null) {
                    switch (status) {
                        case INITIAL:
                            toolbar.setSubtitleTextColor(Color.DKGRAY);
                            break;
                        case CONNECTING:
                            toolbar.setSubtitleTextColor(Color.LTGRAY);
                            break;
                        case CONNECTED:
                            toolbar.setSubtitleTextColor(Color.WHITE);
                            break;
                        case ERROR:
                        case NOTCONNECTED_UNKNOWNREASON:
                        case NOTCONNECTED_USERDISCONNECT:
                            toolbar.setSubtitleTextColor(Color.RED);
                            break;
                    }
                }
            }
        }
    };

    final private BroadcastReceiver broadcastDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MqttClientLocal.MQTTReceivedDataType dataType = (MqttClientLocal.MQTTReceivedDataType) intent.getSerializableExtra(MqttClientLocal.MQTT_DATA_TYPE);
            processMqttData(dataType, intent);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(broadcastStatusReceiver, new IntentFilter(MqttClientLocal.MQTT_STATUS_INTENT));
        registerReceiver(broadcastDataReceiver, new IntentFilter(MqttClientLocal.MQTT_DATA_INTENT));

        //StartServiceIfStopped();
        bindService(new Intent(this, MqttServiceLocal.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(broadcastDataReceiver);
        unregisterReceiver(broadcastStatusReceiver);

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    public void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
    }

    // service

    protected MqttClientLocal getMqttClient() {
        if (mService == null)
            return null;
        return mService.mqttClient();
    }

    public void publish(String topic, String message, boolean retained) {
        MqttClientLocal client = getMqttClient();
        if (client == null)
            return;

        client.publish(topic, message, retained);
    }

    boolean mBound;
    MqttServiceLocal mService = null;

    protected void ServiceConnected() {

    }

//    private void StartServiceIfStopped() {
//        if (!MqttServiceLocal.isRunning()) {
//            startService(new Intent(this, MqttServiceLocal.class));
//        }
//    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            MqttServiceLocal.LocalBinder binder = (MqttServiceLocal.LocalBinder) service;
            mService = binder.getService();

            mBound = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ServiceConnected();
                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            mBound = false;
        }
    };
}
