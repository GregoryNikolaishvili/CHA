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
import android.widget.Toast;

public abstract class ChaActivity extends AppCompatActivity {

    final private BroadcastReceiver broadcastStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String statusMessage = intent.getStringExtra(MqttClient.MQTT_MSG);
            boolean isError = intent.getBooleanExtra(MqttClient.MQTT_MSG_IS_ERROR, false);

            if (isError)
                Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show();

            MqttClient.MQTTConnectionStatus status = (MqttClient.MQTTConnectionStatus) intent.getSerializableExtra(MqttClient.MQTT_CONN_STATUS);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                if (!isError)
                    actionBar.setSubtitle(statusMessage);
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
            MqttClient.MQTTReceivedDataType dataType = (MqttClient.MQTTReceivedDataType) intent.getSerializableExtra(MqttClient.MQTT_DATA_TYPE);
            processMqttData(dataType, intent);
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(broadcastStatusReceiver, new IntentFilter(MqttClient.MQTT_STATUS_INTENT));
        registerReceiver(broadcastDataReceiver, new IntentFilter(MqttClient.MQTT_DATA_INTENT));

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

    void processMqttData(MqttClient.MQTTReceivedDataType dataType, Intent intent) {
    }

    // service

    public MqttClient getMqttClient() {
        return mService.mqttClient();
    }

    boolean mBound;
    MqttServiceLocal mService = null;


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
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            mBound = false;
        }
    };
}
