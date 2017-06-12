package ge.altasoft.gia.cha;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public abstract class ChaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        drawStatus();
    }

    private void drawStatus() {

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        String msg = (Utils.lastMqttConnectionWrtIsOnline ? "✅ " : "✘ ").concat(Utils.lastMqttConnectionStatusMessage);
        if (!Utils.lastMqttConnectionErrorMessage.equals(""))
            msg = msg.concat(", ").concat(Utils.lastMqttConnectionErrorMessage);

        actionBar.setSubtitle(msg);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            switch (Utils.mqttConnectionStatus) {
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

    final private BroadcastReceiver broadcastStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra(MqttClientLocal.MQTT_MSG);
            boolean isError = intent.getBooleanExtra(MqttClientLocal.MQTT_MSG_IS_ERROR, false);

            Utils.mqttConnectionStatus = (MqttClientLocal.MQTTConnectionStatus) intent.getSerializableExtra(MqttClientLocal.MQTT_CONN_STATUS);

            if (isError) {
                Utils.mqttConnectionStatus = MqttClientLocal.MQTTConnectionStatus.ERROR;
                Utils.lastMqttConnectionErrorMessage = msg;
                Utils.lastMqttConnectionErrorMessageTime = System.currentTimeMillis();
            } else {
                Utils.lastMqttConnectionStatusMessage = msg;
                if ((Utils.mqttConnectionStatus == MqttClientLocal.MQTTConnectionStatus.CONNECTED) || ((System.currentTimeMillis() - Utils.lastMqttConnectionErrorMessageTime) > 120000))
                    Utils.lastMqttConnectionErrorMessage = "";
            }

            drawStatus();
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

    protected void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        switch (dataType) {
            case WrtState:
                Utils.lastMqttConnectionWrtIsOnline = intent.getBooleanExtra("value", false);
                drawStatus();
                break;

            case Alert:
                String message = intent.getStringExtra("message");
                if (message != null) {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000);
                }
                break;
        }
    }

    // service

    MqttClientLocal getMqttClient() {
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

    private boolean mBound;
    private MqttServiceLocal mService = null;

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
