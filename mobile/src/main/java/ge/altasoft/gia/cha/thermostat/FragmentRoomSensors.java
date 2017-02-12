package ge.altasoft.gia.cha.thermostat;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Map;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.views.DragLinearLayout;
import ge.altasoft.gia.cha.views.RoomSensorView;

public class FragmentRoomSensors extends Fragment {

    private View rootView = null;
    private DragLinearLayout dragLinearLayout = null;

    public FragmentRoomSensors() {
    }

    public static FragmentRoomSensors newInstance() {
        return new FragmentRoomSensors();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_thermostat_sensors, container, false);

        dragLinearLayout = (DragLinearLayout) rootView.findViewById(R.id.thermostatSensorDragLinearLayout);
        dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition,
                               View secondView, int secondPosition) {

                ThermostatControllerData.Instance.reorderRoomSensorMapping(firstPosition, secondPosition);
            }
        });

        rebuildUI();

        return rootView;
    }

    public void setDraggableViews(boolean on) {
        for (int I = 0; I < dragLinearLayout.getChildCount(); I++) {
            if (dragLinearLayout.getChildAt(I) instanceof LinearLayout) {
                LinearLayout lt = (LinearLayout) dragLinearLayout.getChildAt(I);

                if (on)
                    dragLinearLayout.setViewDraggable(lt, lt);
                else
                    dragLinearLayout.setViewNonDraggable(lt);
            }
        }
    }

    public void checkSensors() {
        if (rootView != null)
            drawStates();
    }

    // rebuild everything and draws new state
    public void rebuildUI() {
        if ((rootView == null) || (ThermostatControllerData.Instance == null) || !ThermostatControllerData.Instance.haveRoomSensorsSettings())
            return;

        View vLoading = dragLinearLayout.findViewById(R.id.thermostatsLoading);
        if (vLoading != null)
            dragLinearLayout.removeView(vLoading);

        dragLinearLayout.removeAllViews();

        Context context = getContext();
        LinearLayout.LayoutParams lpSensor = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        Map<Integer, RoomSensorData> sensors = ThermostatControllerData.Instance.sortedRoomSensors();
        for (int id : sensors.keySet()) {
            RoomSensorData data = ThermostatControllerData.Instance.roomSensors(id, false);

            RoomSensorView sensor = new RoomSensorView(context);
            sensor.setSensorData(data);
            sensor.setLayoutParams(lpSensor);

            dragLinearLayout.addView(sensor);
        }
    }

    public void drawStates() {
        if (rootView == null)
            return;

        for (int i = 0; i < dragLinearLayout.getChildCount(); i++) {
            if (dragLinearLayout.getChildAt(i) instanceof RoomSensorView) {
                RoomSensorView rv = (RoomSensorView) dragLinearLayout.getChildAt(i);
                RoomSensorData data = rv.getSensorData();
                rv.setSensorData(data);
            }
        }
    }

    public void drawState(int id) {
        if (rootView == null)
            return;

        for (int i = 0; i < dragLinearLayout.getChildCount(); i++) {
            if (dragLinearLayout.getChildAt(i) instanceof RoomSensorView) {
                RoomSensorView rv = (RoomSensorView) dragLinearLayout.getChildAt(i);
                RoomSensorData data = rv.getSensorData();
                if (data.getId() == id) {
                    rv.setSensorData(data);
                    break;
                }
            }
        }
    }
}