package ge.altasoft.gia.cha.thermostat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;

import ge.altasoft.gia.cha.GraphActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.TimeAsXAxisLabelFormatter;
import ge.altasoft.gia.cha.views.BoilerPumpView;
import ge.altasoft.gia.cha.views.BoilerSensorView;

public class BoilerFragment extends Fragment {

    public static LineGraphSeries[] graphSeriesStatic;

    private View rootView = null;

    @SuppressWarnings("unchecked")
    private LineGraphSeries<DataPoint>[] graphSeries = new LineGraphSeries[ThermostatControllerData.BOILER_SENSOR_COUNT];

    public BoilerFragment() {
    }

    public static BoilerFragment newInstance() {
        return new BoilerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_boiler, container, false);

        final GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

        graphSeries[ThermostatControllerData.BOILER_SENSOR_SOLAR_PANEL] = new LineGraphSeries<>();
        graphSeries[ThermostatControllerData.BOILER_SENSOR_BOTTOM] = new LineGraphSeries<>();
        graphSeries[ThermostatControllerData.BOILER_SENSOR_TOP] = new LineGraphSeries<>();
        graphSeries[ThermostatControllerData.BOILER_SENSOR_ROOM] = new LineGraphSeries<>();

        graphSeries[ThermostatControllerData.BOILER_SENSOR_SOLAR_PANEL].setColor(Color.RED);
        graphSeries[ThermostatControllerData.BOILER_SENSOR_BOTTOM].setColor(Color.BLUE);
        graphSeries[ThermostatControllerData.BOILER_SENSOR_TOP].setColor(Color.CYAN);
        graphSeries[ThermostatControllerData.BOILER_SENSOR_ROOM].setColor(Color.BLACK);

        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++)
            graph.addSeries(graphSeries[i]);

        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();

        calendar.add(Calendar.MINUTE, 1);
        Date d2 = calendar.getTime();

        graph.getGridLabelRenderer().setLabelFormatter(new TimeAsXAxisLabelFormatter("HH:mm"));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d2.getTime());
        graph.getGridLabelRenderer().setHumanRounding(false);

        graph.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View arg0) {
                graphSeriesStatic = graphSeries;
                Intent intent = new Intent(getActivity(), GraphActivity.class);
                startActivity(intent);
                return true;
            }
        });


        final View boilerLayout = rootView.findViewById(R.id.boilerLayout);
        boilerLayout.getViewTreeObserver().
                addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
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

        if (ThermostatControllerData.Instance.haveSettings())
            rebuildUI();

        return rootView;
    }

    public void rebuildUI() {

    }

    public void drawState() {
        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorSolarPanel)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_SOLAR_PANEL));

        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankBottom)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_BOTTOM));
        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankTop)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_TOP));
        ((BoilerSensorView) rootView.findViewById(R.id.boilerSensorRoom)).setSensorData(ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_ROOM));

        ((BoilerPumpView) rootView.findViewById(R.id.boilerPumpSolarPanel)).setIsOn(ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_SOLAR_PUMP).isOn());
        ((BoilerPumpView) rootView.findViewById(R.id.boilerPumpHeating)).setIsOn(ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_HEATING_PUMP).isOn());

        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();

        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++) {
            graphSeries[i].appendData(new DataPoint(d1.getTime(), ThermostatControllerData.Instance.boilerSensors(i).getTemperature()), true, 40);
        }
    }
}
