package ge.altasoft.gia.cha.thermostat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.ChaActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.ChaFragment;
import ge.altasoft.gia.cha.classes.ItemViewHolder;
import ge.altasoft.gia.cha.views.BoilerFurnaceView;
import ge.altasoft.gia.cha.views.BoilerSensorView;
import ge.altasoft.gia.cha.views.PumpView;

import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER;
import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER_POOL;
import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_WINTER;

public class FragmentBoiler extends ChaFragment {

    private boolean haveLogData = false;

    private GraphicalView mChartView;
    private final XYMultipleSeriesDataset xyDataSet = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer;
    private Date mMaxXX;

    public FragmentBoiler() {
    }

    @Override
    protected boolean canReorder() {
        return false;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_boiler;
    }

    @Override
    protected RecyclerView.Adapter<ItemViewHolder> getRecycleAdapter() {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final CardView cv = ((CardView) rootView.findViewById(R.id.boilerMode));

        cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        ((TextView) cv.getChildAt(0)).setText("⏱"); // ⌛
                        cv.setEnabled(false);

                        switch (item.getItemId()) {
                            case R.id.item_summer:
                                ((ChaActivity) getActivity()).publish("chac/ts/mode", String.valueOf(BOILER_MODE_SUMMER), false);
                                break;
                            case R.id.item_summer_and_pool:
                                ((ChaActivity) getActivity()).publish("chac/ts/mode", String.valueOf(BOILER_MODE_SUMMER_POOL), false);
                                break;
                            case R.id.item_winter:
                                ((ChaActivity) getActivity()).publish("chac/ts/mode", String.valueOf(BOILER_MODE_WINTER), false);
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.boiler_mode_popup_menu);
                popupMenu.show();

                return true;
            }
        });


        final View boilerLayout = rootView.findViewById(R.id.boilerLayout);

        //region relayout
        boilerLayout.getViewTreeObserver().
                addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                              @Override
                                              public void onGlobalLayout() {
                                                  //At this point the layout is complete

                                                  final int boilerImageWidth = 520;
                                                  final int boilerImageHeight = 320;

                                                  final int solarPipePositionX = 110;
                                                  final int solarPipePositionY = 290;

                                                  final int heaterPipePositionX = 420;
                                                  final int heaterPipePositionY = 190;

                                                  final int topSensorPositionY = 130;
                                                  final int bottomSensorPositionY = 235;

                                                  final int solarPanelTopRightX = 130;
                                                  final int solarPanelTopRightY = 28;

                                                  final int solarPanelLeftBottomX = 34;
                                                  final int solarPanelLeftBottomY = 112;

                                                  float scaleX = boilerLayout.getWidth() / (float) boilerImageWidth;
                                                  float scaleY = boilerLayout.getHeight() / (float) boilerImageHeight;

                                                  PumpView pump = (PumpView) rootView.findViewById(R.id.boilerPumpSolarPanel);
                                                  RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) pump.getLayoutParams();
                                                  lp.setMargins(
                                                          Math.round(solarPipePositionX * scaleX - pump.getWidth() / 2f),
                                                          Math.round(solarPipePositionY * scaleY - pump.getHeight() / 2f),
                                                          0,
                                                          0);
                                                  pump.setLayoutParams(lp);

                                                  pump = (PumpView) rootView.findViewById(R.id.boilerPumpHeating);
                                                  lp = (RelativeLayout.LayoutParams) pump.getLayoutParams();
                                                  lp.setMargins(
                                                          Math.round(heaterPipePositionX * scaleX - pump.getWidth() / 2f),
                                                          Math.round(heaterPipePositionY * scaleY - pump.getHeight() / 2f),
                                                          0,
                                                          0);
                                                  pump.setLayoutParams(lp);

                                                  BoilerSensorView sensor = (BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankTop);
                                                  lp = (RelativeLayout.LayoutParams) sensor.getLayoutParams();
                                                  lp.setMargins(
                                                          0,
                                                          Math.round(topSensorPositionY * scaleY - sensor.getHeight() / 2f),
                                                          0,
                                                          0);
                                                  sensor.setLayoutParams(lp);

                                                  sensor = (BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankBottom);
                                                  lp = (RelativeLayout.LayoutParams) sensor.getLayoutParams();
                                                  lp.setMargins(
                                                          0,
                                                          Math.round(bottomSensorPositionY * scaleY - sensor.getHeight() / 2f),
                                                          0,
                                                          0);
                                                  sensor.setLayoutParams(lp);


                                                  // solar panel temperature
                                                  float angleRadians = (float) Math.atan((solarPanelTopRightY - solarPanelLeftBottomY) * scaleY / ((solarPanelTopRightX - solarPanelLeftBottomX) * scaleX));

                                                  sensor = (BoilerSensorView) rootView.findViewById(R.id.boilerSensorSolarPanel);
                                                  lp = (RelativeLayout.LayoutParams) sensor.getLayoutParams();
                                                  lp.setMargins(
                                                          Math.round((solarPanelTopRightX + solarPanelLeftBottomX) / 2f * scaleX + sensor.getHeight() * (float) Math.sin(angleRadians) / 2f - sensor.getWidth() / 2f),
                                                          Math.round((solarPanelTopRightY + solarPanelLeftBottomY) / 2f * scaleY - sensor.getHeight() * (float) Math.cos(angleRadians) / 2f - sensor.getHeight() / 2f),
                                                          0,
                                                          0);
                                                  sensor.setLayoutParams(lp);

                                                  sensor.setPivotX(sensor.getWidth() / 2f);
                                                  sensor.setPivotY(sensor.getHeight() / 2f);
                                                  sensor.setRotation((float) Math.toDegrees(angleRadians));
                                              }
                                          }

                );
        //endregion

        LinearLayout chartLayout = (LinearLayout) rootView.findViewById(R.id.chart);
        XYSeries series1 = new XYSeries("T1");
        XYSeries series2 = new XYSeries("T2");
        XYSeries series3 = new XYSeries("T3");
        XYSeries series4 = new XYSeries("T4");

        mRenderer = ThermostatUtils.getSensorChartRenderer(this.getContext(), true, 4, new int[]{Color.RED, Color.BLUE, Color.CYAN, Color.MAGENTA});
        mRenderer.setPanEnabled(false, false);

        xyDataSet.addSeries(series1);
        xyDataSet.addSeries(series2);
        xyDataSet.addSeries(series3);
        xyDataSet.addSeries(series4);

        mChartView = ChartFactory.getCubeLineChartView(getActivity(), xyDataSet, mRenderer, 0.1f);
        chartLayout.addView(mChartView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mChartView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(), BoilerChartActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mChartView != null)
            mChartView.repaint();
    }

    @Override
    public void checkSensors() {
        if (rootView != null)
            drawSensorAndRelayStates();
    }


    @Override
    public void saveWidgetOrders() {
    }

    @Override
    public void rebuildUI(boolean isStart) {
        if ((rootView == null) || (ThermostatControllerData.Instance == null) || !ThermostatControllerData.Instance.haveBoilerSettings())
            return;

        hideWaitingScreen();

        ((PumpView) rootView.findViewById(R.id.boilerPumpSolarPanel)).setRelayId(ThermostatControllerData.BOILER_SOLAR_PUMP);
        ((PumpView) rootView.findViewById(R.id.boilerPumpHeating)).setRelayId(ThermostatControllerData.BOILER_HEATING_PUMP);

        ((BoilerFurnaceView) rootView.findViewById(R.id.boilerFurnace)).setRelayId(ThermostatControllerData.BOILER_FURNACE);

        drawSensorAndRelayStates();

        if (isStart || !haveLogData) {
            haveLogData = false;
            ((ChaActivity) getActivity()).publish("cha/hub/getlog", "boiler_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
        }
    }

    public void rebuildGraph(String log) {
        haveLogData = true;
        Date[] dates = ThermostatUtils.DrawBoilerSensorChart(log, getNowMinus4Hour(), 30, mChartView, mRenderer, xyDataSet);
        mMaxXX = dates[1];
    }

    private Date getNowMinus4Hour() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -4);
        if (calendar.get(Calendar.MINUTE) >= 30)
            calendar.set(Calendar.MINUTE, 30);
        else
            calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public void drawSensorState(int id) {
        if (rootView == null)
            return;

        int resId;
        switch (id) {
            case ThermostatControllerData.BOILER_SENSOR_SOLAR_PANEL:
                resId = R.id.boilerSensorSolarPanel;
                break;
            case ThermostatControllerData.BOILER_SENSOR_BOTTOM:
                resId = R.id.boilerSensorTankBottom;
                break;
            case ThermostatControllerData.BOILER_SENSOR_TOP:
                resId = R.id.boilerSensorTankTop;
                break;
            case ThermostatControllerData.BOILER_SENSOR_FURNACE:
                resId = R.id.boilerSensorFurnace;
                break;
            default:
                resId = 0;
        }

        if (resId != 0) {
            ((BoilerSensorView) rootView.findViewById(resId)).setSensorData(ThermostatControllerData.Instance.boilerSensors(id));

            BoilerSensorData data = ThermostatControllerData.Instance.boilerSensors(id);
            XYSeries series = xyDataSet.getSeriesAt(id);

            long tm = getNowMinus4Hour().getTime();
            while ((series.getItemCount() > 0) && (series.getX(0) < tm))
                series.remove(0);

            float v = data.getTemperature();
            if (!Float.isNaN(v)) {
                series.add(data.getLastSyncTime(), v);

                if ((mMaxXX != null) && (data.getLastSyncTime() >= mMaxXX.getTime())) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mMaxXX);
                    calendar.add(Calendar.MINUTE, 30);
                    mMaxXX = calendar.getTime();
                    mRenderer.setXAxisMax(mMaxXX.getTime());
                    mRenderer.addXTextLabel(mMaxXX.getTime(), new SimpleDateFormat("HH:mm", Locale.US).format(mMaxXX));
                }
            }

            mChartView.repaint();
        }
    }

    public void drawPumpState(int id) {
        if (rootView == null)
            return;

        int resId;
        switch (id) {
            case ThermostatControllerData.BOILER_SOLAR_PUMP:
                resId = R.id.boilerPumpSolarPanel;
                break;
            case ThermostatControllerData.BOILER_HEATING_PUMP:
                resId = R.id.boilerPumpHeating;
                break;
            case ThermostatControllerData.BOILER_FURNACE:
                resId = R.id.boilerFurnace;
                break;
            //case ThermostatControllerData.BOILER_FURNACE_CIRC_PUMP:
            //    //resId = R.id.boilerFurnace;
            //    break;
            default:
                resId = 0;
        }

        if (resId != 0)
            ((PumpView) rootView.findViewById(resId)).setState(ThermostatControllerData.Instance.boilerPumps(id).getState());

    }

    private void drawSensorAndRelayStates() {
        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorSolarPanel)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_SOLAR_PANEL));

        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankBottom)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_BOTTOM));
        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankTop)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_TOP));
        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorFurnace)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_FURNACE));

        ((PumpView) rootView.findViewById(R.id.boilerPumpSolarPanel)).setState(ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_SOLAR_PUMP).getState());
        ((PumpView) rootView.findViewById(R.id.boilerPumpHeating)).setState(ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_HEATING_PUMP).getState());
        ((PumpView) rootView.findViewById(R.id.boilerFurnace)).setState(ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_FURNACE).getState());

        drawFooter();
    }

    private void drawFooter() {
        CardView cv = ((CardView) rootView.findViewById(R.id.boilerMode));
        ((TextView) cv.getChildAt(0)).setText(ThermostatControllerData.Instance.getBoilerModeText());
        cv.setEnabled(true);
    }
}
