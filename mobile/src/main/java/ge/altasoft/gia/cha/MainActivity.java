package ge.altasoft.gia.cha;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import ge.altasoft.gia.cha.light.LightBroadcastService;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.light.LightRelayData;
import ge.altasoft.gia.cha.light.LightUtils;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatBroadcastService;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatRelayData;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;
import ge.altasoft.gia.cha.views.BoilerPumpView;
import ge.altasoft.gia.cha.views.BoilerSensorView;
import ge.altasoft.gia.cha.views.DragLinearLayout;
import ge.altasoft.gia.cha.views.LightRelayView;
import ge.altasoft.gia.cha.views.RoomSensorView;
import ge.altasoft.gia.cha.views.ThermostatRelayView;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class MainActivity extends AppCompatActivity {

    private Intent lightIntentGet;
    private Intent thermostatIntentGet;

    private Boolean disableOnCheckedListener = false;

    private boolean lightShowSettingsDialog = false;
    private boolean thermostatShowSettingsDialog = false;

    private DragLinearLayout lightDragLinearLayout = null;
    private DragLinearLayout thermostatRelaysDragLinearLayout = null;
    private DragLinearLayout thermostatSensorsDragLinearLayout = null;

    private Menu mainMenu;

    private int leftPane = 0;
    final View[] panels = new ViewGroup[PANEL_COUNT];
    private static OnSwipeTouchListener swipeTouchListener;

    private static final int PANEL_COUNT = 4;

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
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Country House Automation. Starting..");

        final ViewGroup mainLayout = (ViewGroup) findViewById(R.id.content_main);

        panels[0] = findViewById(R.id.lightContainer);
        panels[1] = findViewById(R.id.solarContainer);
        panels[2] = findViewById(R.id.thermostatContainer1);
        panels[3] = findViewById(R.id.thermostatContainer2);

        setLeftPane(0);

        swipeTouchListener = new OnSwipeTouchListener(this) {
//            @Override
//            public void onSwipeDown() {
//                Toast.makeText(ChaApplication.getAppContext(), "Swipe down", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onSwipeUp() {
//                Toast.makeText(ChaApplication.getAppContext(), "Swipe up", Toast.LENGTH_SHORT).show();
//            }

            @Override
            public void onSwipeLeft() {
                setLeftPane(leftPane + 1);
            }

            @Override
            public void onSwipeRight() {
                setLeftPane(leftPane - 1);
            }
        };

        mainLayout.setOnTouchListener(swipeTouchListener);

        lightDragLinearLayout = (DragLinearLayout) findViewById(R.id.lightDragLinearLayout);
        lightDragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition,
                               View secondView, int secondPosition) {

                LightControllerData.Instance.reorderRelayMapping(firstPosition, secondPosition);
            }
        });

        thermostatRelaysDragLinearLayout = (DragLinearLayout) findViewById(R.id.thermostatRelayDragLinearLayout);
        thermostatRelaysDragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition,
                               View secondView, int secondPosition) {

                ThermostatControllerData.Instance.reorderRelayMapping(firstPosition, secondPosition);
            }
        });

        thermostatSensorsDragLinearLayout = (DragLinearLayout) findViewById(R.id.thermostatSensorDragLinearLayout);
        thermostatSensorsDragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition,
                               View secondView, int secondPosition) {

                ThermostatControllerData.Instance.reorderRoomSensorMapping(firstPosition, secondPosition);
            }
        });

        ToggleButton tb = ((ToggleButton) findViewById(R.id.lightsAutoMode));
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (!disableOnCheckedListener) {
                    ((ToggleButton) button).setTextOn("");
                    ((ToggleButton) button).setTextOff("");
                    button.setEnabled(false);
                    LightUtils.sendCommandToController(isChecked ? "A" : "M");
                }
            }
        });

        tb = ((ToggleButton) findViewById(R.id.thermostatAutoMode));
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (!disableOnCheckedListener) {
                    ((ToggleButton) button).setTextOn("");
                    ((ToggleButton) button).setTextOff("");
                    button.setEnabled(false);
                    ThermostatUtils.sendCommandToController(isChecked ? "A" : "M");
                }
            }
        });

        if (LightControllerData.Instance.haveSettings())
            LightControllerData.Instance.rebuildUI(getLightDrawUI());
        if (ThermostatControllerData.Instance.haveSettings())
            ThermostatControllerData.Instance.rebuildUI(getThermostatDrawUI());

        final View boilerLayout = findViewById(R.id.boilerLayout);
        boilerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //At this point the layout is complete

                final int boilerImageWidth = 480;
                final int boilerImageHeight = 312;

                final int solarPipePositionX = 90;
                final int solarPipePositionY = 290;

                final int heaterPipePositionX = 400;
                final int heaterPipePositionY = 190;

                final int topSensorPositionY = 140;
                final int bottomSensorPositionY = 240;

                final int solarPanelTopRightX = 110;
                final int solarPanelTopRightY = 28;

                final int solarPanelLeftBottomX = 14;
                final int solarPanelLeftBottomY = 112;

                float scaleX = boilerLayout.getWidth() / (float) boilerImageWidth;
                float scaleY = boilerLayout.getHeight() / (float) boilerImageHeight;

                BoilerPumpView pump = (BoilerPumpView) findViewById(R.id.boilerPumpSolarPanel);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) pump.getLayoutParams();
                lp.setMargins(
                        Math.round(solarPipePositionX * scaleX - pump.getWidth() / 2f),
                        Math.round(solarPipePositionY * scaleY - pump.getHeight() / 2f),
                        0,
                        0);
                pump.setLayoutParams(lp);

                pump = (BoilerPumpView) findViewById(R.id.boilerPumpHeating);
                lp = (RelativeLayout.LayoutParams) pump.getLayoutParams();
                lp.setMargins(
                        Math.round(heaterPipePositionX * scaleX - pump.getWidth() / 2f),
                        Math.round(heaterPipePositionY * scaleY - pump.getHeight() / 2f),
                        0,
                        0);
                pump.setLayoutParams(lp);

                BoilerSensorView sensor = (BoilerSensorView) findViewById(R.id.boilerSensorTankTop);
                lp = (RelativeLayout.LayoutParams) sensor.getLayoutParams();
                lp.setMargins(
                        0,
                        Math.round(topSensorPositionY * scaleY - sensor.getHeight() / 2f),
                        0,
                        0);
                sensor.setLayoutParams(lp);

                sensor = (BoilerSensorView) findViewById(R.id.boilerSensorTankBottom);
                lp = (RelativeLayout.LayoutParams) sensor.getLayoutParams();
                lp.setMargins(
                        0,
                        Math.round(bottomSensorPositionY * scaleY - sensor.getHeight() / 2f),
                        0,
                        0);
                sensor.setLayoutParams(lp);


                // solar panel temperature
                float angleRadians = (float) Math.atan((solarPanelTopRightY - solarPanelLeftBottomY) * scaleY / ((solarPanelTopRightX - solarPanelLeftBottomX) * scaleX));

                sensor = (BoilerSensorView) findViewById(R.id.boilerSensorSolarPanel);
                lp = (RelativeLayout.LayoutParams) sensor.getLayoutParams();
                lp.setMargins(
                        Math.round((solarPanelTopRightX + solarPanelLeftBottomX) / 2f * scaleX + sensor.getHeight() * (float) Math.sin(angleRadians) / 2f - sensor.getWidth() / 2f),
                        Math.round((solarPanelTopRightY + solarPanelLeftBottomY) / 2f * scaleY - sensor.getHeight() * (float) Math.cos(angleRadians) / 2f - sensor.getHeight() / 2f),
                        0,
                        0);
                sensor.setLayoutParams(lp);

                sensor.setPivotX(sensor.getWidth() / 2f);
                sensor.setPivotY(sensor.getHeight() / 2f);
                sensor.setRotation((float)Math.toDegrees(angleRadians));
            }
        });

        lightIntentGet = new Intent(this, LightBroadcastService.class);
        thermostatIntentGet = new Intent(this, ThermostatBroadcastService.class);
    }


    public void setLeftPane(int leftPane) {
        boolean isLandscape = (getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE);
        int visiblePanes = isLandscape ? 2 : 1;

        if ((leftPane >= 0) && (leftPane <= PANEL_COUNT - visiblePanes)) {
            this.leftPane = leftPane;

            for (int i = 0; i < leftPane; i++)
                panels[i].setVisibility(View.GONE);
            for (int i = leftPane; i < leftPane + visiblePanes; i++)
                panels[i].setVisibility(View.VISIBLE);
            for (int i = leftPane + visiblePanes; i < PANEL_COUNT; i++)
                panels[i].setVisibility(View.GONE);
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

        boolean isChecked;

        switch (id) {
            case R.id.action_ok:
            case R.id.action_cancel:
                this.mainMenu.findItem(R.id.action_ok).setVisible(false);
                this.mainMenu.findItem(R.id.action_cancel).setVisible(false);
                this.mainMenu.findItem(R.id.action_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                for (int I = 2; I < this.mainMenu.size(); I++)
                    this.mainMenu.getItem(I).setEnabled(true);

                setLightDraggableViews(false);
                setThermostatRelayDraggableViews(false);
                setThermostatSensorDraggableViews(false);

                if (id == R.id.action_ok) {
                    if (LightControllerData.Instance.relayOrderChanged())
                        LightUtils.sendCommandToController(LightControllerData.Instance.encodeSettings());
                    if (ThermostatControllerData.Instance.relayOrderChanged() || ThermostatControllerData.Instance.roomSensorOrderChanged())
                        ThermostatUtils.sendCommandToController(ThermostatControllerData.Instance.encodeSettings());
                } else {
                    LightControllerData.Instance.restoreRelayOrders();
                    LightControllerData.Instance.rebuildUI(getLightDrawUI());

                    ThermostatControllerData.Instance.restoreRelayOrders();
                    ThermostatControllerData.Instance.restoreRoomSensorRelayOrders();

                    ThermostatControllerData.Instance.rebuildUI(getThermostatDrawUI());
                }
                return true;

            case R.id.action_refresh:
                LightUtils.sendCommandToController("?");
                return true;

            case R.id.action_show_info:
                RelativeLayout boilerLayout = (RelativeLayout) findViewById(R.id.boilerLayout);
                BoilerPumpView solarPump = (BoilerPumpView) findViewById(R.id.boilerPumpSolarPanel);
                //RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) solarPump.getLayoutParams();

                final int BoilerImageWidth = 480;
                final int BoilerImageHeight = 312;

                final int BoilerSolarPipePositionX = 90;
                final int BoilerSolarPipePositionY = 290;

//        final int PumpHeight = 60;
//        final int PumpPipePositionY = 28;

                lp.setMargins(
                        boilerLayout.getWidth() * BoilerSolarPipePositionX / BoilerImageWidth - solarPump.getWidth() / 2,
                        boilerLayout.getHeight() * BoilerSolarPipePositionY / BoilerImageHeight - solarPump.getHeight() / 2,
                        0,
                        0);
                solarPump.setLayoutParams(lp);

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

                setLightDraggableViews(true);
                setThermostatRelayDraggableViews(true);
                setThermostatSensorDraggableViews(true);
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
            case Utils.ACTIVITY_REQUEST_SETTINGS_CODE:
                if (resultCode == Activity.RESULT_OK) {

                }
                break;
            case LightUtils.ACTIVITY_REQUEST_SETTINGS_CODE:
                if (resultCode == Activity.RESULT_OK)
                    LightBroadcastService.SetAllSettings = resultCode == Activity.RESULT_OK;
                LightControllerData.Instance.rebuildUI(getLightDrawUI());
                break;
            case ThermostatUtils.ACTIVITY_REQUEST_SETTINGS_CODE:
                if (resultCode == Activity.RESULT_OK)
                    ThermostatBroadcastService.SetAllSettings = resultCode == Activity.RESULT_OK;
                ThermostatControllerData.Instance.rebuildUI(getThermostatDrawUI());
                break;
        }
    }


    //region Light
    private void setLightDraggableViews(boolean on) {
        for (int I = 0; I < lightDragLinearLayout.getChildCount(); I++) {
            LinearLayout lt = (LinearLayout) lightDragLinearLayout.getChildAt(I);

            if (on)
                lightDragLinearLayout.setViewDraggable(lt, lt);
            else
                lightDragLinearLayout.setViewNonDraggable(lt);
        }
    }

    private void processLightControllerData(Intent intent) {
        String response = intent.getStringExtra("response");
        if (LightControllerData.Instance.decode(response, getLightDrawUI())) {
            if (lightShowSettingsDialog) {
                lightShowSettingsDialog = false;
                startActivityForResult(new Intent(this, LightSettingsActivity.class), LightUtils.ACTIVITY_REQUEST_SETTINGS_CODE);
            }
        }
    }
//    private TextView getLightLabel(int i) {
//        LinearLayout lt = (LinearLayout) lightDragLinearLayout.getChildAt(i);
//        return (TextView) lt.getChildAt(0);
//    }

    private TextView getLightComment(int i) {
        LinearLayout lt = (LinearLayout) lightDragLinearLayout.getChildAt(i);
        return (TextView) lt.getChildAt(1);
    }

    private ToggleButton getLightButton(int i) {
        LinearLayout lt = (LinearLayout) lightDragLinearLayout.getChildAt(i);
        return (ToggleButton) lt.getChildAt(2);
    }

    @NonNull
    private RelayControllerData.IDrawRelaysUI getLightDrawUI() {
        return new RelayControllerData.IDrawRelaysUI() {

            public void createNewRelay(final RelayData relayData) {
                Context context = ChaApplication.getAppContext();
                LinearLayout.LayoutParams lpRelay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                LightRelayView relayView = new LightRelayView(context);
                relayView.setLightRelayData((LightRelayData) relayData);
                relayView.setLayoutParams(lpRelay);

                lightDragLinearLayout.addView(relayView);
            }

            public void clearAllRelays() {
                lightDragLinearLayout.removeAllViews();
            }

            public void drawFooterRelays() {
                ToggleButton tvAuto = ((ToggleButton) findViewById(R.id.lightsAutoMode));
                disableOnCheckedListener = true;
                try {
                    tvAuto.setTextOn(getResources().getString(R.string.auto));
                    tvAuto.setTextOff(getResources().getString(R.string.manual));
                    tvAuto.setChecked(LightControllerData.Instance.isActive());
                    tvAuto.setEnabled(true);
                } finally {
                    disableOnCheckedListener = false;
                }

                ((TextView) findViewById(R.id.lightsTimeTextView)).setText(LightControllerData.Instance.GetStatusText());
            }
        };
    }
    //endregion

    //region Thermostat

    private void setThermostatRelayDraggableViews(boolean on) {
        for (int I = 0; I < thermostatRelaysDragLinearLayout.getChildCount(); I++) {
            LinearLayout lt = (LinearLayout) thermostatRelaysDragLinearLayout.getChildAt(I);

            if (on)
                thermostatRelaysDragLinearLayout.setViewDraggable(lt, lt);
            else
                thermostatRelaysDragLinearLayout.setViewNonDraggable(lt);
        }
    }

    private void setThermostatSensorDraggableViews(boolean on) {
        for (int I = 0; I < thermostatSensorsDragLinearLayout.getChildCount(); I++) {
            LinearLayout lt = (LinearLayout) thermostatSensorsDragLinearLayout.getChildAt(I);

            if (on)
                thermostatSensorsDragLinearLayout.setViewDraggable(lt, lt);
            else
                thermostatSensorsDragLinearLayout.setViewNonDraggable(lt);
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


    @NonNull
    private ThermostatControllerData.IDrawThermostatUI getThermostatDrawUI() {
        return new ThermostatControllerData.IDrawThermostatUI() {

            public void createNewRelay(RelayData relayData) {
                Context context = ChaApplication.getAppContext();
                LinearLayout.LayoutParams lpRelay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                ThermostatRelayView relay = new ThermostatRelayView(context);
                relay.setThermostatRelayData((ThermostatRelayData) relayData);
                relay.setLayoutParams(lpRelay);

                thermostatRelaysDragLinearLayout.addView(relay);
            }

            public void clearAllRelays() {
                thermostatRelaysDragLinearLayout.removeAllViews();
            }

            public void drawFooterRelays() {
                ToggleButton tvAuto = ((ToggleButton) findViewById(R.id.thermostatAutoMode));
                disableOnCheckedListener = true;
                try {
                    tvAuto.setTextOn(getResources().getString(R.string.active));
                    tvAuto.setTextOff(getResources().getString(R.string.off));
                    tvAuto.setChecked(ThermostatControllerData.Instance.isActive());
                    tvAuto.setEnabled(true);
                } finally {
                    disableOnCheckedListener = false;
                }

                ((TextView) findViewById(R.id.thermostatTimeTextView1)).setText(ThermostatControllerData.Instance.GetStatusText());
                ((TextView) findViewById(R.id.thermostatTimeTextView2)).setText(ThermostatControllerData.Instance.GetStatusText());
            }

            public void createNewSensor(RoomSensorData roomSensorData) {
                Context context = ChaApplication.getAppContext();
                LinearLayout.LayoutParams lpSensor = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                RoomSensorView sensor = new RoomSensorView(context);
                sensor.setSensorData(roomSensorData);
                roomSensorData.setRoomSensorView(sensor);
                sensor.setLayoutParams(lpSensor);

                thermostatSensorsDragLinearLayout.addView(sensor);
            }

            public void clearAllSensors() {
                thermostatSensorsDragLinearLayout.removeAllViews();
            }

            public void resetBoiler() {
                ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_SOLAR_PANEL).setBoilerSensorView((BoilerSensorView) findViewById(R.id.boilerSensorSolarPanel));
                ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_BOTTOM).setBoilerSensorView((BoilerSensorView) findViewById(R.id.boilerSensorTankBottom));
                ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_TOP).setBoilerSensorView((BoilerSensorView) findViewById(R.id.boilerSensorTankTop));
                ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_ROOM).setBoilerSensorView((BoilerSensorView) findViewById(R.id.boilerSensorRoom));

                ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_SOLAR_PUMP).setBoilerPumpView((BoilerPumpView) findViewById(R.id.boilerPumpSolarPanel));
                ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_HEATING_PUMP).setBoilerPumpView((BoilerPumpView) findViewById(R.id.boilerPumpHeating));
            }
        };
    }

    //endregion

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
}
