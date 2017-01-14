package ge.altasoft.gia.cha;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import ge.altasoft.gia.cha.light.LightBroadcastService;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.light.LightFragment;
import ge.altasoft.gia.cha.light.LightSettingsActivity;
import ge.altasoft.gia.cha.light.LightUtils;
import ge.altasoft.gia.cha.thermostat.BoilerFragment;
import ge.altasoft.gia.cha.thermostat.ThermostatSensorsFragment;
import ge.altasoft.gia.cha.thermostat.ThermostatBroadcastService;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatRelaysFragment;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class MainActivity extends ChaActivity {

    private Intent lightIntent;
    private Intent thermostatIntent;

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

        lightIntent = new Intent(this, LightBroadcastService.class);
        thermostatIntent = new Intent(this, ThermostatBroadcastService.class);
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
                    pagerAdapter.lightFragment.rebuildUI();

                    ThermostatControllerData.Instance.restoreRelayOrders();
                    ThermostatControllerData.Instance.restoreRoomSensorRelayOrders();
                    pagerAdapter.thermostatSensorsFragment.rebuildUI();
                    pagerAdapter.thermostatRelaysFragment.rebuildUI();
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
                startActivityForResult(new Intent(this, LightSettingsActivity.class), LightUtils.ACTIVITY_REQUEST_SETTINGS_CODE);
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
                startActivityForResult(new Intent(this, LightSettingsActivity.class), ThermostatUtils.ACTIVITY_REQUEST_SETTINGS_CODE);
                return true;

            //endregion
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        startService(lightIntent);
        startService(thermostatIntent);

    }

    @Override
    public void onResume() {
        super.onResume();

        if (pagerAdapter.lightFragment != null)
            pagerAdapter.lightFragment.rebuildUI();

        if (pagerAdapter.boilerFragment != null)
            pagerAdapter.boilerFragment.rebuildUI();
        if (pagerAdapter.thermostatSensorsFragment != null)
            pagerAdapter.thermostatSensorsFragment.rebuildUI();
        if (pagerAdapter.thermostatRelaysFragment != null)
            pagerAdapter.thermostatRelaysFragment.rebuildUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopService(thermostatIntent);
        stopService(lightIntent);
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
                pagerAdapter.lightFragment.rebuildUI();
                break;
            case ThermostatUtils.ACTIVITY_REQUEST_SETTINGS_CODE:
                if (resultCode == Activity.RESULT_OK)
                    ThermostatBroadcastService.SetAllSettings = true;
                pagerAdapter.boilerFragment.rebuildUI();
                pagerAdapter.thermostatSensorsFragment.rebuildUI();
                pagerAdapter.thermostatRelaysFragment.rebuildUI();
                break;
        }
    }

    @Override
    protected void processLightControllerData(int flags) {
        super.processLightControllerData(flags);

        if ((flags & Utils.FLAG_HAVE_SETTINGS) != 0)
            pagerAdapter.lightFragment.rebuildUI();
        else if ((flags & Utils.FLAG_HAVE_STATE) != 0) {
            pagerAdapter.lightFragment.drawState();
        }
    }

    @Override
    protected void processThermostatControllerData(int flags) {
        super.processThermostatControllerData(flags);

        if ((flags & Utils.FLAG_HAVE_SETTINGS) != 0) {
            pagerAdapter.boilerFragment.rebuildUI();
            pagerAdapter.thermostatSensorsFragment.rebuildUI();
            pagerAdapter.thermostatRelaysFragment.rebuildUI();
        } else if ((flags & Utils.FLAG_HAVE_STATE) != 0) {
            pagerAdapter.boilerFragment.drawState();
            pagerAdapter.thermostatSensorsFragment.drawState();
            pagerAdapter.thermostatRelaysFragment.drawState();
        }
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
