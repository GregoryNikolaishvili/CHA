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

import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.light.FragmentLight;
import ge.altasoft.gia.cha.light.LightSettingsActivity;

import ge.altasoft.gia.cha.thermostat.FragmentBoiler;
import ge.altasoft.gia.cha.thermostat.FragmentRoomSensors;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.FragmentHeaterRelays;
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

    Handler timerHandler = new Handler();

    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            if (pagerAdapter.fragmentBoiler != null)
                pagerAdapter.fragmentBoiler.checkSensors();
            if (pagerAdapter.fragmentRoomSensors != null)
                pagerAdapter.fragmentRoomSensors.checkSensors();

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

                pagerAdapter.fragmentLight.setDraggableViews(false);
                pagerAdapter.fragmentHeaterRelays.setDraggableViews(false);
                pagerAdapter.fragmentRoomSensors.setDraggableViews(false);

                if (id == R.id.action_ok) {
                    if (LightControllerData.Instance.relayOrderChanged())
                        publish("chac/light/settings/names", LightControllerData.Instance.encodeNamesAndOrder(), false);
                    if (ThermostatControllerData.Instance.roomSensorOrderChanged())
                        publish("chac/ts/settings/rs/names", ThermostatControllerData.Instance.encodeRoomSensorNamesAndOrder(), false);
                } else {
                    LightControllerData.Instance.restoreRelayOrders();
                    pagerAdapter.fragmentLight.rebuildUI();

                    ThermostatControllerData.Instance.restoreRelayOrders();
                    ThermostatControllerData.Instance.restoreRoomSensorRelayOrders();
                    pagerAdapter.fragmentRoomSensors.rebuildUI();
                    pagerAdapter.fragmentHeaterRelays.rebuildUI();
                }
                return true;

            case R.id.action_refresh:
                publish("chac/light/refresh", "1", false);
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

            case R.id.action_light_reorder:
                LightControllerData.Instance.saveRelayOrders();
                ThermostatControllerData.Instance.saveRelayOrders();
                ThermostatControllerData.Instance.saveRoomSensorOrders();

                this.mainMenu.findItem(R.id.action_ok).setVisible(true);
                this.mainMenu.findItem(R.id.action_cancel).setVisible(true);
                this.mainMenu.findItem(R.id.action_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

                for (int I = 2; I < this.mainMenu.size(); I++)
                    this.mainMenu.getItem(I).setEnabled(false);

                pagerAdapter.fragmentLight.setDraggableViews(true);
                pagerAdapter.fragmentHeaterRelays.setDraggableViews(true);
                pagerAdapter.fragmentRoomSensors.setDraggableViews(true);
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

        if (pagerAdapter.fragmentLight != null)
            pagerAdapter.fragmentLight.rebuildUI();

        if (pagerAdapter.fragmentBoiler != null)
            pagerAdapter.fragmentBoiler.rebuildUI(true);
        if (pagerAdapter.fragmentRoomSensors != null)
            pagerAdapter.fragmentRoomSensors.rebuildUI();
        if (pagerAdapter.fragmentHeaterRelays != null)
            pagerAdapter.fragmentHeaterRelays.rebuildUI();

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
                        sb.append("CMX Maximum limited collector temperature (collector cooling function)");
                        sb.append("\n\r");
                    }
                    if ((state & Utils.ERR_SMX) != 0) {
                        sb.append("SMX Maximum temperature of tank");
                        sb.append("\n\r");
                    }

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
                        image = (ImageView) findViewById(R.id.lightControllerIsOnline);
                        value = intent.getBooleanExtra("value", false);
                        if (image != null)
                            image.setImageResource(value ? R.drawable.circle_green : R.drawable.circle_red);
                        break;
                    case "TS controller":
                        image = (ImageView) findViewById(R.id.tsControllerIsOnline);
                        value = intent.getBooleanExtra("value", false);
                        if (image != null)
                            image.setImageResource(value ? R.drawable.circle_green : R.drawable.circle_red);
                        break;
                }
                break;

            case LightSettings:
            case LightNameAndOrders:
                pagerAdapter.fragmentLight.rebuildUI();
                break;

            case LightRelayState:
                id = intent.getIntExtra("id", -1);
                pagerAdapter.fragmentLight.drawState(id);
                break;

            case ThermostatRoomSensorSettings:
            case ThermostatRoomSensorNameAndOrders:
                pagerAdapter.fragmentRoomSensors.rebuildUI();
                break;

            case ThermostatBoilerSettings:
                pagerAdapter.fragmentBoiler.rebuildUI(false);
                break;

            case Log:
                if (intent.getStringExtra("type").startsWith("boiler"))
                    pagerAdapter.fragmentBoiler.rebuildGraph(intent.getStringExtra("log"));
                break;

//            case ThermostatHeaterRelaySettings:
//            case ThermostatHeaterRelayNameAndOrders:
//                pagerAdapter.fragmentHeaterRelays.rebuildUI();
//                break;

            case ThermostatRoomSensorState:
                id = intent.getIntExtra("id", -1);
                if (intent.getBooleanExtra("new_sensor", false))
                    pagerAdapter.fragmentRoomSensors.rebuildUI();

                pagerAdapter.fragmentRoomSensors.drawState(id);
                break;

            case ThermostatBoilerSensorState:
                id = intent.getIntExtra("id", -1);

                pagerAdapter.fragmentBoiler.drawSensorState(id);
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

        FragmentLight fragmentLight = null;
        FragmentBoiler fragmentBoiler = null;
        FragmentRoomSensors fragmentRoomSensors = null;
        FragmentHeaterRelays fragmentHeaterRelays = null;

        SectionsPagerAdapter(FragmentManager fm, boolean isLandscape) {
            super(fm);

            this.isLandscape = isLandscape;

            fragmentLight = FragmentLight.newInstance();
            fragmentBoiler = FragmentBoiler.newInstance();
            fragmentRoomSensors = FragmentRoomSensors.newInstance();
            fragmentHeaterRelays = FragmentHeaterRelays.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return fragmentLight;
                case 1:
                    return fragmentBoiler;
                case 2:
                    return fragmentRoomSensors;
                case 3:
                    return fragmentHeaterRelays;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Lights";
                case 1:
                    return "Solar";
                case 2:
                    return "T & H sensors";
                case 3:
                    return "Heater valves";
            }
            return null;
        }

        @Override
        public float getPageWidth(int position) {
            return (isLandscape ? 0.5f : 1.0f);
        }
    }
}
