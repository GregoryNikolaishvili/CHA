package ge.altasoft.gia.cha.thermostat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;

import java.util.Date;

import ge.altasoft.gia.cha.GraphActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.CircularArrayList;
import ge.altasoft.gia.cha.classes.LineSeriesArray;
import ge.altasoft.gia.cha.views.BoilerPumpView;
import ge.altasoft.gia.cha.views.BoilerSensorView;

public class FragmentBoiler extends Fragment {

    private View rootView = null;

    private LineSeriesArray pointSeries = new LineSeriesArray(ThermostatControllerData.BOILER_SENSOR_COUNT);

    public FragmentBoiler() {
    }

    public static FragmentBoiler newInstance() {
        return new FragmentBoiler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_boiler, container, false);

        final GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
        pointSeries.addToGraph(graph);

        graph.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(), GraphActivity.class);
                startActivity(intent);
            }
        });


        ToggleButton tb = ((ToggleButton) rootView.findViewById(R.id.boilerMode));
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (!Utils.disableOnCheckedListener) {
                    ((ToggleButton) button).setTextOn("");
                    ((ToggleButton) button).setTextOff("");
                    button.setEnabled(false);
                    ThermostatUtils.sendCommandToController(getContext(), "X");
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

        rebuildUI();

        return rootView;
    }

    // rebuild everything and draws new state
    public void rebuildUI() {
        if (!ThermostatControllerData.Instance.haveSettings() || (rootView == null))
            return;

        ((BoilerPumpView) rootView.findViewById(R.id.boilerPumpSolarPanel)).setRelayId(ThermostatControllerData.BOILER_SOLAR_PUMP);
        ((BoilerPumpView) rootView.findViewById(R.id.boilerPumpHeating)).setRelayId(ThermostatControllerData.BOILER_HEATING_PUMP);

        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++) {
            CircularArrayList<Pair<Date, Float>> points = ThermostatControllerData.Instance.boilerSensors(i).getLogBuffer();

            DataPoint[] dataPoints = new DataPoint[points.size()];
            int idx = 0;
            for (Pair<Date, Float> pt : points)
                dataPoints[idx++] = new DataPoint(pt.first.getTime(), pt.second);

            pointSeries.getItem(i).resetData(dataPoints);
        }

        drawSensorAndRelayStates();
    }

    public void drawState() {
        if (rootView == null)
            return;

        drawSensorAndRelayStates();

        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++) {
            pointSeries.getItem(i).append(ThermostatControllerData.Instance.boilerSensors(i));
        }
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
            tvAuto.setChecked(ThermostatControllerData.Instance.getBoilerMode() != ThermostatControllerData.BOILER_MODE_OFF);
            tvAuto.setEnabled(true);
        } finally {
            Utils.disableOnCheckedListener = false;
        }

        ((TextView) rootView.findViewById(R.id.boilerTimeTextView)).setText(ThermostatControllerData.Instance.GetStatusText());
    }
}