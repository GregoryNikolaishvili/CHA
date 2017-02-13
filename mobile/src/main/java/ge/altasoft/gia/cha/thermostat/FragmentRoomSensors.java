package ge.altasoft.gia.cha.thermostat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.ItemTouchHelperAdapter;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.SimpleItemTouchHelperCallback;
import ge.altasoft.gia.cha.views.RoomSensorView;

public class FragmentRoomSensors extends Fragment implements OnStartDragListener {

    private ViewGroup rootView = null;
    private boolean dragMode = false;
    private TextView tvLoading;
    private ItemTouchHelper mItemTouchHelper;

    public FragmentRoomSensors() {
    }

    public static FragmentRoomSensors newInstance() {
        return new FragmentRoomSensors();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_thermostat_sensors, container, false);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tvLoading = new TextView(getContext());

        tvLoading.setLayoutParams(lp);
        tvLoading.setGravity(Gravity.CENTER);
        tvLoading.setText(getResources().getString(R.string.loading));
        rootView.addView(tvLoading, 0);

        rebuildUI();

        return rootView;
    }

    private class MySimpleItemTouchHelperCallback extends SimpleItemTouchHelperCallback {
        public MySimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            super(adapter);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return dragMode;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RoomSensorRecyclerListAdapter adapter = new RoomSensorRecyclerListAdapter(this);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.thermostatRecyclerView);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 4);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.Callback callback = new MySimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        if (dragMode)
            mItemTouchHelper.startDrag(viewHolder);
    }

    public void setDraggableViews(boolean on) {
        dragMode = on;
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.thermostatRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RoomSensorView view = (RoomSensorView) recyclerView.getChildAt(i);
            view.setDragMode(on);
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

        if (tvLoading != null) {
            rootView.removeView(tvLoading);
            tvLoading = null;
        }

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.thermostatRecyclerView);

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RoomSensorView sensor = (RoomSensorView) recyclerView.getChildAt(i);
            if (sensor != null) {
                RoomSensorData data = ThermostatControllerData.Instance.getRoomSensorFromUIIndex(i);
                sensor.setSensorData(data);
            }
        }
    }

    public void drawStates() {
        if (rootView == null)
            return;

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.thermostatRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RoomSensorView sensor = (RoomSensorView) recyclerView.getChildAt(i);
            if (sensor != null) {
                RoomSensorData data = sensor.getSensorData();
                sensor.setSensorData(data);
            }
        }
    }


    public void drawState(int id) {
        if (rootView == null)
            return;

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.thermostatRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RoomSensorView sensor = (RoomSensorView) recyclerView.getChildAt(i);
            RoomSensorData data = sensor.getSensorData();
            if (data.getId() == id) {
                sensor.setSensorData(data);
                break;
            }
        }
    }
}