package ge.altasoft.gia.cha;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Map;

import ge.altasoft.gia.cha.classes.ChaFragment;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.light.FragmentLight;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.light.LightSettingsActivity;
import ge.altasoft.gia.cha.other.FragmentOtherSensors;
import ge.altasoft.gia.cha.other.OtherControllerData;
import ge.altasoft.gia.cha.other.WaterLevelSettingsActivity;
import ge.altasoft.gia.cha.thermostat.FragmentBoiler;
import ge.altasoft.gia.cha.thermostat.FragmentRoomSensors;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatSettingsActivity;

public class MainActivity extends ChaActivity {

    private final Handler timerHandler = new Handler();
    private SectionsPagerAdapter pagerAdapter;
    private final Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            for (int i = 0; i < pagerAdapter.getCount(); i++)
                ((ChaFragment) pagerAdapter.getItem(i)).checkSensors();

            timerHandler.postDelayed(this, 60000);
        }
    };
    private Menu mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isTablet = Utils.isTablet(this);
        if (isTablet)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        boolean keepScreenOn = prefs.getBoolean("keepScreenOn", true);

        if (keepScreenOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Country House Automation");

        pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), Utils.isTablet(this));

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setOffscreenPageLimit(8);
        viewPager.setAdapter(pagerAdapter);

        if (savedInstanceState == null) {
            if (isTablet)
                viewPager.setCurrentItem(0);
            else
                viewPager.setCurrentItem(1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.mainMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_ok:
            case R.id.action_cancel:
                this.mainMenu.findItem(R.id.action_ok).setVisible(false);
                this.mainMenu.findItem(R.id.action_cancel).setVisible(false);
                this.mainMenu.findItem(R.id.action_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                for (int I = 2; I < this.mainMenu.size(); I++)
                    this.mainMenu.getItem(I).setEnabled(true);

                for (int i = 0; i < pagerAdapter.getCount(); i++)
                    ((ChaFragment) pagerAdapter.getItem(i)).setDraggableViews(false);


                if (id == R.id.action_ok) {
                    for (int i = 0; i < pagerAdapter.getCount(); i++)
                        ((ChaFragment) pagerAdapter.getItem(i)).saveWidgetOrders();
                } else
                    rebuildUI(false);

                return true;

            case R.id.action_reorder_widgets:
                this.mainMenu.findItem(R.id.action_ok).setVisible(true);
                this.mainMenu.findItem(R.id.action_cancel).setVisible(true);
                this.mainMenu.findItem(R.id.action_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

                for (int I = 2; I < this.mainMenu.size(); I++)
                    this.mainMenu.getItem(I).setEnabled(false);

                for (int i = 0; i < pagerAdapter.getCount(); i++)
                    ((ChaFragment) pagerAdapter.getItem(i)).setDraggableViews(true);

                return true;


            case R.id.action_refresh:
                publish("chac/lc/refresh", "1", false);
                publish("chac/ts/refresh", "1", false);
                publish("chac/wl/refresh", "1", false);
                return true;

            case R.id.action_show_info:
                showNetworkInfo();
                return true;

            case R.id.who_is_online:
                Intent intent = new Intent(this, WhoIsOnlineActivity.class);
                intent.putStringArrayListExtra("list", getMqttClient().getConnectedClientList());
                startActivity(intent);
                return true;

            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), Utils.ACTIVITY_REQUEST_RESULT_SETTINGS);
                return true;

            case R.id.action_light_settings:
                startActivityForResult(new Intent(this, LightSettingsActivity.class), Utils.ACTIVITY_REQUEST_RESULT_LIGHT_SETTINGS);
                return true;

            case R.id.action_thermostat_settings:
                startActivityForResult(new Intent(this, ThermostatSettingsActivity.class), Utils.ACTIVITY_REQUEST_RESULT_THERMOSTAT_SETTINGS);
                return true;

            case R.id.action_water_level_settings:
                startActivityForResult(new Intent(this, WaterLevelSettingsActivity.class), Utils.ACTIVITY_REQUEST_RESULT_WATER_LEVEL_SETTINGS);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        rebuildUI(true);

        timerHandler.postDelayed(timerRunnable, 60000);

        //Utils.analyseStorage(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();

        drawControllerStatus(false, R.id.lcControllerIsOnline);
        drawControllerStatus(false, R.id.lcControllerIsOnline2);
        drawControllerStatus(false, R.id.tsControllerIsOnline);
        drawControllerStatus(false, R.id.tsControllerIsOnline2);
        drawControllerStatus(false, R.id.tsControllerIsOnline3);
        drawControllerStatus(false, R.id.wlControllerIsOnline);
        drawControllerStatus(false, R.id.wlControllerIsOnline2);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
//            case Utils.ACTIVITY_REQUEST_RESULT_SETTINGS:
//                if (resultCode == Activity.RESULT_OK) {
//
//                }
//                break;

            case Utils.ACTIVITY_REQUEST_RESULT_LIGHT_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    publish("chac/lc/settings", LightControllerData.Instance.encodeSettings(), false);
                    publish("chac/lc/settings/names", LightControllerData.Instance.encodeNamesAndOrder(), false);
                }
                clearUnneededPreferences();
                break;

            case Utils.ACTIVITY_REQUEST_RESULT_THERMOSTAT_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    publish("chac/ts/settings/rs", ThermostatControllerData.Instance.encodeRoomSensorSettings(), false);
                    publish("chac/ts/settings/bl", ThermostatControllerData.Instance.encodeBoilerSettings(), false);
                    publish("chac/ts/settings/rs/names", ThermostatControllerData.Instance.encodeRoomSensorNamesAndOrder(), false);
                }
                clearUnneededPreferences();
                break;

            case Utils.ACTIVITY_REQUEST_RESULT_WATER_LEVEL_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    publish("chac/wl/settings", OtherControllerData.Instance.encodeWaterLevelSettings(), false);
                    publish("chac/wl/settings/names", OtherControllerData.Instance.encodeNamesAndOrder(), false);
                }
                clearUnneededPreferences();
                break;
        }
    }

    private void clearUnneededPreferences() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (!(key.equals("mtqq_url_local") || key.equals("mtqq_url_global") || key.equals("dashboard_items")))
                editor.remove(key);
        }
        editor.apply();
    }

    private void rebuildUI(boolean isStart) {
        for (int i = 0; i < pagerAdapter.getCount(); i++)
            ((ChaFragment) pagerAdapter.getItem(i)).rebuildUI(isStart);
    }

    @Override
    public void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        int id;
        int state;
        long boardTimeInSec;
        StringBuilder sb;

        switch (dataType) {
            case WrtState:
                drawWrtStatus(Utils.lastMqttConnectionWrtIsOnline, R.id.wrtIsOnlineDash);
                drawWrtStatus(Utils.lastMqttConnectionWrtIsOnline, R.id.wrtIsOnlineLC);
                drawWrtStatus(Utils.lastMqttConnectionWrtIsOnline, R.id.wrtIsOnlineTS);
                drawWrtStatus(Utils.lastMqttConnectionWrtIsOnline, R.id.wrtIsOnlineOther);
                break;

            case ClientConnected:
                String clientId = intent.getStringExtra("id");
                boolean value;
                switch (clientId) {
                    case "LC controller":
                        value = intent.getBooleanExtra("value", false);
                        drawControllerStatus(value && LightControllerData.Instance.isAlive(), R.id.lcControllerIsOnline);
                        drawControllerStatus(value && LightControllerData.Instance.isAlive(), R.id.lcControllerIsOnline2);
                        break;
                    case "TS controller":
                        value = intent.getBooleanExtra("value", false);
                        drawControllerStatus(value && ThermostatControllerData.Instance.isAlive(), R.id.tsControllerIsOnline);
                        drawControllerStatus(value && ThermostatControllerData.Instance.isAlive(), R.id.tsControllerIsOnline2);
                        drawControllerStatus(value && ThermostatControllerData.Instance.isAlive(), R.id.tsControllerIsOnline3);
                        break;
                    case "WL controller":
                        value = intent.getBooleanExtra("value", false);
                        drawControllerStatus(value && OtherControllerData.Instance.isAlive(), R.id.wlControllerIsOnline);
                        drawControllerStatus(value && OtherControllerData.Instance.isAlive(), R.id.wlControllerIsOnline2);
                        break;
                }
                break;

            case LightControllerAlive:
                boardTimeInSec = intent.getLongExtra("BoardTimeInSec", 0);
                LightControllerData.Instance.SetAlive(boardTimeInSec);
                drawControllerStatus(LightControllerData.Instance.isAlive(), R.id.lcControllerIsOnline);
                drawControllerStatus(LightControllerData.Instance.isAlive(), R.id.lcControllerIsOnline2);
                break;

            //region Controller states
            case LightControllerState:
                sb = new StringBuilder();
                state = intent.getIntExtra("state", 0);

                if (state != 0) {
                    if ((state & Utils.ERR_GENERAL) != 0) {
                        sb.append("General error");
                        sb.append("\r\n");
                    }

                    if (sb.length() >= 2) // delete last \r\n
                        sb.setLength(sb.length() - 2);

                    if (pagerAdapter.fragmentDashboard != null)
                        pagerAdapter.fragmentDashboard.drawControllersState("LC", sb);

                    Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000);
                } else if (pagerAdapter.fragmentDashboard != null)
                    pagerAdapter.fragmentDashboard.drawControllersState("LC", null);

                break;

            case ThermostatControllerAlive:
                boardTimeInSec = intent.getLongExtra("BoardTimeInSec", 0);
                ThermostatControllerData.Instance.SetAlive(boardTimeInSec);
                drawControllerStatus(ThermostatControllerData.Instance.isAlive(), R.id.tsControllerIsOnline);
                drawControllerStatus(ThermostatControllerData.Instance.isAlive(), R.id.tsControllerIsOnline2);
                drawControllerStatus(ThermostatControllerData.Instance.isAlive(), R.id.tsControllerIsOnline3);
                break;

            case ThermostatControllerState:
                sb = new StringBuilder();
                state = intent.getIntExtra("state", 0);

                if (state != 0) {
                    if ((state & Utils.ERR_GENERAL) != 0) {
                        sb.append("General error");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_SENSOR) != 0) {
                        sb.append("Sensor error");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_EMOF) != 0) {
                        sb.append("Emergency switch-off temperature of collector");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_95_DEGREE) != 0) {
                        sb.append("Tank emergency temperature (95)");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_CFR) != 0) {
                        sb.append("CFR Antifreeze function activated");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_SMX) != 0) {
                        sb.append("SMX Maximum temperature of tank");
                        sb.append("\r\n");
                    }

                    if ((state & Utils.ERR_T1) != 0) {
                        sb.append("Solar sensor fail (T1)");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_T2) != 0) {
                        sb.append("Boiler sensor fail (T2)");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_T3) != 0) {
                        sb.append("Boiler sensor fail (T3)");
                        sb.append("\r\n");
                    }

                    if ((state & Utils.ERR_TF) != 0) {
                        sb.append("Furnace sensor fail (TF)");
                        sb.append("\r\n");
                    }

                    if (sb.length() >= 2) // delete last \r\n
                        sb.setLength(sb.length() - 2);

                    if (pagerAdapter.fragmentDashboard != null)
                        pagerAdapter.fragmentDashboard.drawControllersState("TS", sb);

                    Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000);
                } else if (pagerAdapter.fragmentDashboard != null)
                    pagerAdapter.fragmentDashboard.drawControllersState("TS", null);

                break;

            case WaterLevelControllerAlive:
                boardTimeInSec = intent.getLongExtra("BoardTimeInSec", 0);
                OtherControllerData.Instance.SetAlive(boardTimeInSec);
                drawControllerStatus(OtherControllerData.Instance.isAlive(), R.id.wlControllerIsOnline);
                drawControllerStatus(OtherControllerData.Instance.isAlive(), R.id.wlControllerIsOnline2);
                break;

            case WaterLevelControllerState:
                sb = new StringBuilder();
                state = intent.getIntExtra("state", 0);

                if (state != 0) {
                    if ((state & Utils.ERR_GENERAL) != 0) {
                        sb.append("General error");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_ULTRASONIC_1) != 0) {
                        sb.append("Ultrasonic sensor #1 error");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_ULTRASONIC_2) != 0) {
                        sb.append("Ultrasonic sensor #2 error");
                        sb.append("\r\n");
                    }
                    if ((state & Utils.ERR_ULTRASONIC_3) != 0) {
                        sb.append("Ultrasonic sensor #3 error");
                        sb.append("\r\n");
                    }

                    if (sb.length() >= 2) // delete last \r\n
                        sb.setLength(sb.length() - 2);

                    if (pagerAdapter.fragmentDashboard != null)
                        pagerAdapter.fragmentDashboard.drawControllersState("WL", sb);

                    Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000);
                } else if (pagerAdapter.fragmentDashboard != null)
                    pagerAdapter.fragmentDashboard.drawControllersState("WL", null);

                break;
            //endregion

            case LightSettings:
            case LightNameAndOrders:
                pagerAdapter.fragmentLight.rebuildUI(false);
                pagerAdapter.fragmentDashboard.rebuildUI(false);
                break;

            case LightRelayState:
                id = intent.getIntExtra("id", -1);
                pagerAdapter.fragmentLight.drawState(id);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.LightRelay, id);
                break;

            case ThermostatRoomSensorSettings:
            case ThermostatRoomSensorNameAndOrders:
                pagerAdapter.fragmentRoomSensors.rebuildUI(false);
                pagerAdapter.fragmentDashboard.rebuildUI(false);
                break;

            case ThermostatBoilerSettings:
                pagerAdapter.fragmentBoiler.rebuildUI(false);
                pagerAdapter.fragmentDashboard.rebuildUI(false);
                break;

            case Log:
                if (intent.getStringExtra("type").startsWith("boiler"))
                    pagerAdapter.fragmentBoiler.rebuildGraph(intent.getStringExtra("log"));
                break;


            case SensorRoomState:
                id = intent.getIntExtra("id", -1);
                if (intent.getBooleanExtra("new_sensor", false))
                    pagerAdapter.fragmentRoomSensors.rebuildUI(false);

                pagerAdapter.fragmentRoomSensors.drawState(id);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.RoomSensor, id);
                break;

            case Sensor5in1StateW:
                pagerAdapter.fragmentOtherSensors.drawState(WidgetType.WindSensor, OtherControllerData._5IN1_SENSOR_ID_WIND);
                pagerAdapter.fragmentOtherSensors.drawState(WidgetType.PressureSensor, OtherControllerData._5IN1_SENSOR_ID_PRESSURE);
                pagerAdapter.fragmentOtherSensors.drawState(WidgetType.RainSensor, OtherControllerData._5IN1_SENSOR_ID_RAIN);
                pagerAdapter.fragmentOtherSensors.drawState(WidgetType.WindDirSensor, OtherControllerData._5IN1_SENSOR_ID_WIND_DIR);

                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.WindSensor, OtherControllerData._5IN1_SENSOR_ID_WIND);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.PressureSensor, OtherControllerData._5IN1_SENSOR_ID_PRESSURE);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.RainSensor, OtherControllerData._5IN1_SENSOR_ID_RAIN);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.WindDirSensor, OtherControllerData._5IN1_SENSOR_ID_WIND_DIR);
                break;

            case ThermostatBoilerSensorState:
                id = intent.getIntExtra("id", -1);

                pagerAdapter.fragmentBoiler.drawSensorState(id);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.BoilerSensor, id);
                break;

            case ThermostatBoilerPumpState:
                id = intent.getIntExtra("id", -1);

                pagerAdapter.fragmentBoiler.drawPumpState(id);
                break;

            case WaterLevelState:
                id = intent.getIntExtra("id", -1);

                pagerAdapter.fragmentOtherSensors.drawState(WidgetType.WaterLevelSensor, id);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.WaterLevelSensor, id);
                break;

            case WaterLevelRelayState:
                id = intent.getIntExtra("id", -1);
                pagerAdapter.fragmentOtherSensors.drawState(WidgetType.WaterLevelPumpRelay, id);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.WaterLevelPumpRelay, id);
                break;

            case WaterLevelSettings:
            case WaterLevelNameAndOrders:
                pagerAdapter.fragmentOtherSensors.rebuildUI(false);
                pagerAdapter.fragmentDashboard.rebuildUI(false);
                break;
        }
    }

    private void drawControllerStatus(boolean isOK, int resId) {
        ImageView image = (ImageView) findViewById(resId);
        if (image != null) {
            image.setImageResource(isOK ? R.drawable.circle_green : R.drawable.circle_red);
        }
    }

    private void drawWrtStatus(boolean isOK, int resId) {
        ImageView image = (ImageView) findViewById(resId);
        if (image != null) {
            image.setImageResource(isOK ? R.drawable.wifi_on : R.drawable.wifi_off);
        }
    }

//    private void redrawControllerStatus(int resId) {
//        ImageView image = (ImageView) findViewById(resId);
//        if ((image != null) && (image.getTag() != null)) {
//            boolean value = (boolean) image.getTag();
//            drawControllerStatus(value, resId);
//        }
//    }


    private void showNetworkInfo() {
        StringBuilder sb = new StringBuilder();

        String info = Utils.getNetworkInfo(this);
        if (info != null)
            sb.append(info);

        String url = Utils.getMtqqBrokerUrl(this);
        if (url == null)
            url = "(null)";
        sb.append("\nUrl - ");
        sb.append(url);

        Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
    }

    private static class SectionsPagerAdapter extends FragmentPagerAdapter {

        final private boolean isLandscape;

        FragmentDashboard fragmentDashboard = null;
        FragmentBoiler fragmentBoiler = null;
        FragmentLight fragmentLight = null;
        FragmentRoomSensors fragmentRoomSensors = null;
        FragmentOtherSensors fragmentOtherSensors = null;

        SectionsPagerAdapter(FragmentManager fm, boolean isLandscape) {
            super(fm);

            this.isLandscape = isLandscape;

            fragmentDashboard = new FragmentDashboard();
            fragmentBoiler = new FragmentBoiler();
            fragmentLight = new FragmentLight();
            fragmentRoomSensors = new FragmentRoomSensors();
            fragmentOtherSensors = new FragmentOtherSensors();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return fragmentBoiler;
                case 1:
                    return fragmentDashboard;
                case 2:
                    return fragmentOtherSensors;
                case 3:
                    return fragmentRoomSensors;
                case 4:
                    return fragmentLight;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Boiler";
                case 1:
                    return "Dashboard";
                case 2:
                    return "Other sensors";
                case 3:
                    return "Room sensors";
                case 4:
                    return "Lights";
            }
            return null;
        }

        @Override
        public float getPageWidth(int position) {
            return (isLandscape ? 0.5f : 1.0f);
        }
    }
}
