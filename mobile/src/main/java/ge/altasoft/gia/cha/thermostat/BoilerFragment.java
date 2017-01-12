package ge.altasoft.gia.cha.thermostat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.views.BoilerPumpView;
import ge.altasoft.gia.cha.views.BoilerSensorView;

public class BoilerFragment extends Fragment {

    private View rootView = null;
    private LineGraphSeries series0;

    public BoilerFragment() {
    }

    public static BoilerFragment newInstance() {
        return new BoilerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_boiler, container, false);

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
        series0 = new LineGraphSeries<>();
        graph.addSeries(series0);

        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();

        calendar.add(Calendar.MINUTE, 1);
        Date d2 = calendar.getTime();

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d2.getTime());
        graph.getGridLabelRenderer().setHumanRounding(false);

        final View boilerLayout = rootView.findViewById(R.id.boilerLayout);
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
        });

        return rootView;
    }


    public void resetBoiler() {
        ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_SOLAR_PANEL).setBoilerSensorView((BoilerSensorView) rootView.findViewById(R.id.boilerSensorSolarPanel));
        ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_BOTTOM).setBoilerSensorView((BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankBottom));
        ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_TOP).setBoilerSensorView((BoilerSensorView) rootView.findViewById(R.id.boilerSensorTankTop));
        ThermostatControllerData.Instance.boilerSensors(ThermostatControllerData.BOILER_SENSOR_ROOM).setBoilerSensorView((BoilerSensorView) rootView.findViewById(R.id.boilerSensorRoom));

        ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_SOLAR_PUMP).setBoilerPumpView((BoilerPumpView) rootView.findViewById(R.id.boilerPumpSolarPanel));
        ThermostatControllerData.Instance.boilerPumps(ThermostatControllerData.BOILER_HEATING_PUMP).setBoilerPumpView((BoilerPumpView) rootView.findViewById(R.id.boilerPumpHeating));
    }

    public void drawGraph(int id, BoilerSensorData boilerSensorData) {
        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();

        if (id == 0)
            series0.appendData(new DataPoint(d1.getTime(), boilerSensorData.getTemperature()), true, 40);
    }
}
