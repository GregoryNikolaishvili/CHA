package ge.altasoft.gia.cha;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import ge.altasoft.gia.cha.light.LightBroadcastService;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.light.LightFragment;
import ge.altasoft.gia.cha.light.LightUtils;
import ge.altasoft.gia.cha.thermostat.BoilerFragment;
import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatSensorsFragment;
import ge.altasoft.gia.cha.thermostat.ThermostatBroadcastService;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatRelaysFragment;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class MainActivity extends AppCompatActivity {

    private Intent lightIntentGet;
    private Intent thermostatIntentGet;

    private boolean lightShowSettingsDialog = false;
    private boolean thermostatShowSettingsDialog = false;

    private SectionsPagerAdapter pagerAdapter;

    private Menu mainMenu;

    private BroadcastReceiver lightBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            processLightControllerData(intent);
        }
    };

    private BroadcastReceiver thermostatBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            processThermostatControllerData(intent);
        }
    };

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

        if (LightControllerData.Instance.haveSettings())
            LightControllerData.Instance.rebuildUI(pagerAdapter.lightFragment);

        if (ThermostatControllerData.Instance.haveSettings())
            ThermostatControllerData.Instance.rebuildUI(getThermostatDrawUI());

        lightIntentGet = new Intent(this, LightBroadcastService.class);
        thermostatIntentGet = new Intent(this, ThermostatBroadcastService.class);
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

                pagerAdapter.lightFragment.setDraggableViews(false);
                pagerAdapter.thermostatRelaysFragment.setDraggableViews(false);
                pagerAdapter.thermostatSensorsFragment.setDraggableViews(false);

                if (id == R.id.action_ok) {
                    if (LightControllerData.Instance.relayOrderChanged())
                        LightUtils.sendCommandToController(LightControllerData.Instance.encodeSettings());
                    if (ThermostatControllerData.Instance.relayOrderChanged() || ThermostatControllerData.Instance.roomSensorOrderChanged())
                        ThermostatUtils.sendCommandToController(ThermostatControllerData.Instance.encodeSettings());
                } else {
                    LightControllerData.Instance.restoreRelayOrders();
                    LightControllerData.Instance.rebuildUI(pagerAdapter.lightFragment);

                    ThermostatControllerData.Instance.restoreRelayOrders();
                    ThermostatControllerData.Instance.restoreRoomSensorRelayOrders();
                    ThermostatControllerData.Instance.rebuildUI(getThermostatDrawUI());
                }
                return true;

            case R.id.action_refresh:
                LightUtils.sendCommandToController("?");
                return true;

            case R.id.action_show_info:
                showNetworkInfo();
                return true;

            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), Utils.ACTIVITY_REQUEST_SETTINGS_CODE);
                return true;

            //region Light
            case R.id.action_light_settings:
                lightShowSettingsDialog = true;
                LightUtils.sendCommandToController("@");
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

                pagerAdapter.lightFragment.setDraggableViews(true);
                pagerAdapter.thermostatRelaysFragment.setDraggableViews(true);
                pagerAdapter.thermostatSensorsFragment.setDraggableViews(true);
                return true;

            //region Thermostat
            case R.id.action_thermostat_settings:
                thermostatShowSettingsDialog = true;
                ThermostatUtils.sendCommandToController("@");
                return true;

            //endregion
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        startService(lightIntentGet);
        startService(thermostatIntentGet);

        registerReceiver(lightBroadcastReceiver, new IntentFilter(LightBroadcastService.BROADCAST_ACTION_GET));
        registerReceiver(thermostatBroadcastReceiver, new IntentFilter(ThermostatBroadcastService.BROADCAST_ACTION_GET));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(thermostatBroadcastReceiver);
        unregisterReceiver(lightBroadcastReceiver);

        stopService(thermostatIntentGet);
        stopService(lightIntentGet);
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
            case LightUtils.ACTIVITY_REQUEST_SETTINGS_CODE:
                if (resultCode == Activity.RESULT_OK)
                    LightBroadcastService.SetAllSettings = true;
                LightControllerData.Instance.rebuildUI(pagerAdapter.lightFragment);
                break;
            case ThermostatUtils.ACTIVITY_REQUEST_SETTINGS_CODE:
                if (resultCode == Activity.RESULT_OK)
                    ThermostatBroadcastService.SetAllSettings = true;
                ThermostatControllerData.Instance.rebuildUI(getThermostatDrawUI());
                break;
        }
    }

    private void processLightControllerData(Intent intent) {
        String response = intent.getStringExtra("response");
        if (LightControllerData.Instance.decode(response, pagerAdapter.lightFragment)) {
            if (lightShowSettingsDialog) {
                lightShowSettingsDialog = false;
                startActivityForResult(new Intent(this, LightSettingsActivity.class), LightUtils.ACTIVITY_REQUEST_SETTINGS_CODE);
            }
        }
    }

    private void processThermostatControllerData(Intent intent) {

        String response = intent.getStringExtra("response");

        if (ThermostatControllerData.Instance.decode(response, getThermostatDrawUI())) {
            if (thermostatShowSettingsDialog) {
                thermostatShowSettingsDialog = false;
                startActivityForResult(new Intent(this, LightSettingsActivity.class), ThermostatUtils.ACTIVITY_REQUEST_SETTINGS_CODE);
            }
        }
    }

    private ThermostatControllerData.IDrawThermostatUI getThermostatDrawUI() {
        return new ThermostatControllerData.IDrawThermostatUI() {

            public void createNewRelay(RelayData relayData) {
                pagerAdapter.thermostatRelaysFragment.createNewRelay(relayData);
            }

            public void clearAllRelays() {
                pagerAdapter.thermostatRelaysFragment.clearAllRelays();
            }

            public void drawFooterRelays() {
                pagerAdapter.thermostatRelaysFragment.drawFooterRelays();
            }

            public void createNewSensor(RoomSensorData roomSensorData) {
                pagerAdapter.thermostatSensorsFragment.createNewSensor(roomSensorData);
            }

            public void clearAllSensors() {
                pagerAdapter.thermostatSensorsFragment.clearAllSensors();
            }

            public void resetBoiler() {
                pagerAdapter.boilerFragment.resetBoiler();
            }

            public void drawChart(int id, BoilerSensorData boilerSensorData)
            {
                pagerAdapter.boilerFragment.drawGraph(id, boilerSensorData);
            }
        };
    }


    private void showNetworkInfo() {
        StringBuilder sb = new StringBuilder();

        String info = Utils.GetNetworkInfo(this);
        if (info != null)
            sb.append(info);

        String ip = LightUtils.GetControllerIp(this);
        if (ip == null)
            ip = "(null)";
        sb.append("\nLight - ");
        sb.append(ip);
        sb.append(':');
        sb.append(LightUtils.ServerPort);

        ip = ThermostatUtils.GetControllerIp(this);
        if (ip == null)
            ip = "(null)";
        sb.append("\nThermostat - ");
        sb.append(ip);
        sb.append(':');
        sb.append(ThermostatUtils.ServerPort);

        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        private boolean isLandscape;

        LightFragment lightFragment = null;
        BoilerFragment boilerFragment = null;
        ThermostatSensorsFragment thermostatSensorsFragment = null;
        ThermostatRelaysFragment thermostatRelaysFragment = null;


        SectionsPagerAdapter(FragmentManager fm, boolean isLandscape) {
            super(fm);

            this.isLandscape = isLandscape;

            lightFragment = LightFragment.newInstance();
            boilerFragment = BoilerFragment.newInstance();
            thermostatSensorsFragment = ThermostatSensorsFragment.newInstance();
            thermostatRelaysFragment = ThermostatRelaysFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return lightFragment;
                case 1:
                    return boilerFragment;
                case 2:
                    return thermostatSensorsFragment;
                case 3:
                    return thermostatRelaysFragment;
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
