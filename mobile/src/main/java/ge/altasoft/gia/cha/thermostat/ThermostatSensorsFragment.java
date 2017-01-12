package ge.altasoft.gia.cha.thermostat;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ge.altasoft.gia.cha.ChaApplication;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.views.DragLinearLayout;
import ge.altasoft.gia.cha.views.RoomSensorView;

public class ThermostatSensorsFragment extends Fragment {

    public DragLinearLayout thermostatSensorsDragLinearLayout = null;

    public ThermostatSensorsFragment() {
    }

    public static ThermostatSensorsFragment newInstance() {
        return new ThermostatSensorsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_thermostat_sensors, container, false);

        thermostatSensorsDragLinearLayout = (DragLinearLayout) rootView.findViewById(R.id.thermostatSensorDragLinearLayout);
        thermostatSensorsDragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition,
                               View secondView, int secondPosition) {

                ThermostatControllerData.Instance.reorderRelayMapping(firstPosition, secondPosition);
            }
        });

        return rootView;
    }

    public void setDraggableViews(boolean on) {
        for (int I = 0; I < thermostatSensorsDragLinearLayout.getChildCount(); I++) {
            LinearLayout lt = (LinearLayout) thermostatSensorsDragLinearLayout.getChildAt(I);

            if (on)
                thermostatSensorsDragLinearLayout.setViewDraggable(lt, lt);
            else
                thermostatSensorsDragLinearLayout.setViewNonDraggable(lt);
        }
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
}
