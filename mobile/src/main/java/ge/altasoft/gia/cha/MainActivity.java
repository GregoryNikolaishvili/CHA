package ge.altasoft.gia.cha;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import ge.altasoft.gia.cha.classes.ChaFragment;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.light.FragmentLight;
import ge.altasoft.gia.cha.light.LightSettingsActivity;

import ge.altasoft.gia.cha.other.FragmentOtherSensors;
import ge.altasoft.gia.cha.other.OtherControllerData;
import ge.altasoft.gia.cha.thermostat.FragmentBoiler;
import ge.altasoft.gia.cha.thermostat.FragmentRoomSensors;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatSettingsActivity;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class MainActivity extends ChaActivity {

    private SectionsPagerAdapter pagerAdapter;
    private Menu mainMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.isTablet(this))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Country House Automation");

        pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), Utils.isTablet(this));

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setOffscreenPageLimit(8);
        viewPager.setAdapter(pagerAdapter);
    }

    private final Handler timerHandler = new Handler();

    private final Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            for (int i = 0; i < pagerAdapter.getCount(); i++)
                ((ChaFragment) pagerAdapter.getItem(i)).checkSensors();

            timerHandler.postDelayed(this, 60000);
        }
    };

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
                //publish("chac/light/refresh", "1", false);
                publish("chac/ts/refresh", "1", false);
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
                startActivityForResult(new Intent(this, SettingsActivity.class), Utils.ACTIVITY_REQUEST_SETTINGS_CODE);
                return true;

            //region Light
            case R.id.action_light_settings:
                startActivityForResult(new Intent(this, LightSettingsActivity.class), Utils.ACTIVITY_REQUEST_RESULT_LIGHT_SETTINGS);
                return true;

            //region Thermostat
            case R.id.action_thermostat_settings:
                startActivityForResult(new Intent(this, ThermostatSettingsActivity.class), ThermostatUtils.ACTIVITY_REQUEST_SETTINGS_CODE);
                return true;

            //endregion
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        rebuildUI(true);

        timerHandler.postDelayed(timerRunnable, 60000);
    }

    @Override
    protected void onPause() {
        super.onPause();

        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
//            case Utils.ACTIVITY_REQUEST_SETTINGS_CODE:
//                if (resultCode == Activity.RESULT_OK) {
//
//                }
//                break;

            case Utils.ACTIVITY_REQUEST_RESULT_LIGHT_SETTINGS:
                if (resultCode == Activity.RESULT_OK) {
                    publish("chac/light/settings/names", LightControllerData.Instance.encodeNamesAndOrder(), false);
                    publish("chac/light/settings", LightControllerData.Instance.encodeSettings(), false);
                }
                break;

            case ThermostatUtils.ACTIVITY_REQUEST_SETTINGS_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    publish("chac/ts/settings/rs", ThermostatControllerData.Instance.encodeRoomSensorSettings(), false);
                    publish("chac/ts/settings/bl", ThermostatControllerData.Instance.encodeBoilerSettings(), false);

                    publish("chac/ts/settings/rs/names", ThermostatControllerData.Instance.encodeRoomSensorNamesAndOrder(), false);
                }
                break;
        }
    }

    private void rebuildUI(boolean isStart) {
        for (int i = 0; i < pagerAdapter.getCount(); i++)
            ((ChaFragment) pagerAdapter.getItem(i)).rebuildUI(isStart);
    }

    @Override
    public void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        int id;

        switch (dataType) {
//            case Alert:
//                String message = intent.getStringExtra("message");
//                if (message == null) {
//                }
//
//                break;

            case ThermostatState:
                StringBuilder sb = new StringBuilder();
                int state = intent.getIntExtra("state", 0);

                if (state != 0) {
                    if ((state & Utils.ERR_GENERAL) != 0) {
                        sb.append("General error");
                        sb.append("\n\r");
                    }
                    if ((state & Utils.ERR_SENSOR) != 0) {
                        sb.append("Sensor error");
                        sb.append("\n\r");
                    }
                    if ((state & Utils.ERR_EMOF) != 0) {
                        sb.append("Emergency switch-off temperature of collector");
                        sb.append("\n\r");
                    }
                    if ((state & Utils.ERR_95_DEGREE) != 0) {
                        sb.append("Tank emergency temperature (95)");
                        sb.append("\n\r");
                    }
                    if ((state & Utils.ERR_CMX) != 0) {
                        sb.append("CMX Maximum limited collector temperature");
                        sb.append("\n\r");
                    }
                    if ((state & Utils.ERR_SMX) != 0) {
                        sb.append("SMX Maximum temperature of tank");
                        sb.append("\n\r");
                    }

                    if (pagerAdapter.fragmentBoiler != null)
                        pagerAdapter.fragmentBoiler.drawThermostatState(sb);

                    Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 3000);
                }
                break;

            case ClientConnected:
                String clientId = intent.getStringExtra("id");
                ImageView image;
                boolean value;
                switch (clientId) {
                    case "Lights controller":
                        value = intent.getBooleanExtra("value", false);
                        image = (ImageView) findViewById(R.id.lightControllerIsOnline);
                        if (image != null)
                            image.setImageResource(value ? R.drawable.circle_green : R.drawable.circle_red);
                        image = (ImageView) findViewById(R.id.lightControllerIsOnline2);
                        if (image != null)
                            image.setImageResource(value ? R.drawable.circle_green : R.drawable.circle_red);
                        break;
                    case "TS controller":
                        value = intent.getBooleanExtra("value", false);
                        image = (ImageView) findViewById(R.id.tsControllerIsOnline);
                        if (image != null)
                            image.setImageResource(value ? R.drawable.circle_green : R.drawable.circle_red);
                        image = (ImageView) findViewById(R.id.tsControllerIsOnline2);
                        if (image != null)
                            image.setImageResource(value ? R.drawable.circle_green : R.drawable.circle_red);
                        break;
                }
                break;

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

            case Sensor5in1StateTH:
                pagerAdapter.fragmentOtherSensors.drawState(OtherControllerData._5IN1_SENSOR_ID_TH);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.OutsideSensor, OtherControllerData._5IN1_SENSOR_ID_TH);
                break;

            case Sensor5in1StateW:
                pagerAdapter.fragmentOtherSensors.drawState(OtherControllerData._5IN1_SENSOR_ID_WIND);
                pagerAdapter.fragmentOtherSensors.drawState(OtherControllerData._5IN1_SENSOR_ID_PRESSURE);
                pagerAdapter.fragmentOtherSensors.drawState(OtherControllerData._5IN1_SENSOR_ID_RAIN);

                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.WindSensor, OtherControllerData._5IN1_SENSOR_ID_WIND);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.PressureSensor, OtherControllerData._5IN1_SENSOR_ID_PRESSURE);
                pagerAdapter.fragmentDashboard.drawWidgetState(WidgetType.RainSensor, OtherControllerData._5IN1_SENSOR_ID_RAIN);
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
        }
    }

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

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

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
                    return fragmentDashboard;
                case 1:
                    return fragmentBoiler;
                case 2:
                    return fragmentLight;
                case 3:
                    return fragmentRoomSensors;
                case 4:
                    return fragmentOtherSensors;
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
                    return "Dashboard";
                case 1:
                    return "Boiler";
                case 2:
                    return "Lights";
                case 3:
                    return "Room sensors";
                case 4:
                    return "Other sensors";
            }
            return null;
        }

        @Override
        public float getPageWidth(int position) {
            return (isLandscape ? 0.5f : 1.0f);
        }
    }
}
