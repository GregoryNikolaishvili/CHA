package ge.altasoft.gia.cha.thermostat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.BoilerPumpView;
import ge.altasoft.gia.cha.views.BoilerSensorView;

public class FragmentBoiler extends Fragment {

    private View rootView = null;

    private boolean haveLogData = false;

    private GraphicalView mChartView;
    private XYMultipleSeriesDataset xyDataSet = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer;
    private Date mMaxXX;

    public FragmentBoiler() {
    }

    public static FragmentBoiler newInstance() {
        return new FragmentBoiler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_boiler, container, false);

        ToggleButton tb = ((ToggleButton) rootView.findViewById(R.id.boilerMode));
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (!Utils.disableOnCheckedListener) {
                    ((ToggleButton) button).setTextOn("");
                    ((ToggleButton) button).setTextOff("");
                    button.setEnabled(false);
                    ((ChaActivity) getActivity()).publish("chac/ts/mode", String.valueOf(ThermostatControllerData.Instance.nextBoilerMode()), false);
                }
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

                                                  final int topSensorPositionY = 140;
                                                  final int bottomSensorPositionY = 240;

                                                  final int solarPanelTopRightX = 130;
                                                  final int solarPanelTopRightY = 28;

                                                  final int solarPanelLeftBottomX = 34;
                                                  final int solarPanelLeftBottomY = 112;

                                                  float scaleX = boilerLayout.getWidth() / (float) boilerImageWidth;
                                                  float scaleY = boilerLayout.getHeight() / (float) boilerImageHeight;

                                                  BoilerPumpView pump = (BoilerPumpView) rootView.findViewById(R.id.boilerPumpSolarPanel);
                                                  RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) pump.getLayoutParams();
                                                  lp.setMargins(
                                                          Math.round(solarPipePositionX * scaleX - pump.getWidth() / 2f),
                                                          Math.round(solarPipePositionY * scaleY - pump.getHeight() / 2f),
                                                          0,
                                                          0);
                                                  pump.setLayoutParams(lp);

                                                  pump = (BoilerPumpView) rootView.findViewById(R.id.boilerPumpHeating);
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

        mRenderer = ThermostatUtils.getChartRenderer(4, new int[]{Color.RED, Color.BLUE, Color.CYAN, Color.MAGENTA});
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

        rebuildUI(false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mChartView != null)
            mChartView.repaint();
    }

    public void checkSensors() {
        if (rootView != null)
            drawSensorAndRelayStates();
    }

    // rebuild everything and draws new state
    public void rebuildUI(boolean requestGraphLog) {
        if ((rootView == null) || (ThermostatControllerData.Instance == null) || !ThermostatControllerData.Instance.haveBoilerSettings())
            return;

        ((BoilerPumpView) rootView.findViewById(R.id.boilerPumpSolarPanel)).setRelayId(ThermostatControllerData.BOILER_SOLAR_PUMP);
        ((BoilerPumpView) rootView.findViewById(R.id.boilerPumpHeating)).setRelayId(ThermostatControllerData.BOILER_HEATING_PUMP);

        drawSensorAndRelayStates();

        if (requestGraphLog || !haveLogData) {
            haveLogData = false;
            ((ChaActivity) getActivity()).publish("cha/hub/getlog", "boiler_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
        }
    }

    public void rebuildGraph(String log) {

        haveLogData = true;

        Date[] dates = ThermostatUtils.DrawSensorChart(-1, "BoilerSensor", log, getNowMinus4Hour(), 30, mChartView, mRenderer, xyDataSet);
        //double mMinXX = dates[0].getTime();
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

//    public void drawState() {
//        if (rootView == null)
//            return;
//
//        drawSensorAndRelayStates();
//
//        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++) {
//            pointSeries.getItem(i).append(ThermostatControllerData.Instance.boilerSensors(i));
//        }
//    }

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
            case ThermostatControllerData.BOILER_SENSOR_ROOM:
                resId = R.id.boilerSensorRoom;
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
                series.add(data.getLastReadingTime(), v);

                if ((mMaxXX != null) && (data.getLastReadingTime() >= mMaxXX.getTime())) {
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
            default:
                resId = 0;
        }

        if (resId != 0)
            ((BoilerPumpView) rootView.findViewById(resId)).setIsOn(ThermostatControllerData.Instance.boilerPumps(id).isOn());

    }

    private void drawSensorAndRelayStates() {
        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorSolarPanel)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_SOLAR_PANEL));

        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankBottom)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_BOTTOM));
        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankTop)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_TOP));
        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorRoom)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_ROOM));

        ((BoilerPumpView) rootView.findViewById(R.id.boilerPumpSolarPanel)).setIsOn(ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_SOLAR_PUMP).isOn());
        ((BoilerPumpView) rootView.findViewById(R.id.boilerPumpHeating)).setIsOn(ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_HEATING_PUMP).isOn());

        drawFooter();
    }

    private void drawFooter() {
        ToggleButton tvAuto = ((ToggleButton) rootView.findViewById(R.id.boilerMode));
        Utils.disableOnCheckedListener = true;
        try {
            tvAuto.setTextOn(ThermostatControllerData.Instance.getBoilerModeText());
            tvAuto.setTextOff(getResources().getString(R.string.off));
            tvAuto.setChecked(ThermostatControllerData.Instance.getBoilerMode() != BoilerSettings.BOILER_MODE_OFF);
            tvAuto.setEnabled(true);
        } finally {
            Utils.disableOnCheckedListener = false;
        }

        ((TextView) rootView.findViewById(R.id.boilerTimeTextView)).setText(ThermostatControllerData.Instance.GetStatusText());
    }
}
