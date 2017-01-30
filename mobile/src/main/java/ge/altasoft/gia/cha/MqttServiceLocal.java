package ge.altasoft.gia.cha;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MqttServiceLocal extends Service {

    private MqttClient mqttClient = null;

    private final IBinder mBinder = new MqttServiceLocal.LocalBinder();

    public class LocalBinder extends Binder {
        MqttServiceLocal getService() {
            // Return this instance of LocalService so clients can call public methods
            return MqttServiceLocal.this;
        }
    }

    public MqttServiceLocal() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return super.onUnbind(intent);
    }

    public MqttClient mqttClient() {
        return this.mqttClient;
    }

    //private static boolean isRunning = false;

    //public static boolean isRunning() {
    //return isRunning;
    //}

//    private void clientUnBound()
//    {
//        clientCount--;
//        if (clientCount <= 0)
//        {
//            stopSelf();
//
//            if (getMqttClient != null) {
//                getMqttClient.stop();
//            }
//        }
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MQTTService", "Service Started");

        //isRunning = true;

        mqttClient = new MqttClient(this);
        mqttClient.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
