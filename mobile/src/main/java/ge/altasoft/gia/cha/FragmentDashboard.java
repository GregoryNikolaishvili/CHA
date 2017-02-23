package ge.altasoft.gia.cha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import ge.altasoft.gia.cha.classes.ChaCard;
import ge.altasoft.gia.cha.classes.DashboardItems;
import ge.altasoft.gia.cha.classes.ItemTouchHelperAdapter;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.SimpleItemTouchHelperCallback;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.light.LightRelayData;
import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.views.BoilerSensorView;
import ge.altasoft.gia.cha.views.LightRelayView;
import ge.altasoft.gia.cha.views.RoomSensorView;

public class FragmentDashboard extends Fragment implements OnStartDragListener {

    private ViewGroup rootView = null;
    private boolean dragMode = false;
    private TextView tvLoading;
    private ItemTouchHelper mItemTouchHelper;
    DashboardRecyclerListAdapter adapter;

    public FragmentDashboard() {
    }

    public static FragmentDashboard newInstance() {
        return new FragmentDashboard();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_dashboard, container, false);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tvLoading = new TextView(getContext());

        tvLoading.setLayoutParams(lp);
        tvLoading.setGravity(Gravity.CENTER);
        tvLoading.setText(getResources().getString(R.string.loading));
        rootView.addView(tvLoading, 0);

        DashboardItems.restore(getContext());

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

        adapter = new DashboardRecyclerListAdapter(this);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.dashboardRecyclerView);
        recyclerView.setHasFixedSize(true);
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
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.dashboardRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ChaCard view = (ChaCard) recyclerView.getChildAt(i);
            view.setDragMode(on);
        }
    }

    // rebuild everything and draws new state
    public void rebuildUI() {
        if (rootView == null) return;

        if ((LightControllerData.Instance == null) || !LightControllerData.Instance.haveSettings())
            return;
        if ((ThermostatControllerData.Instance == null) || !ThermostatControllerData.Instance.haveBoilerSettings())
            return;

        if (tvLoading != null) {
            rootView.removeView(tvLoading);
            tvLoading = null;
        }

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.dashboardRecyclerView);

        drawFooter();
    }

    private void drawFooter() {
    }

    public void drawLightState(int id) {
        if (rootView == null)
            return;

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.dashboardRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            if (recyclerView.getChildAt(i) instanceof LightRelayView) {
                LightRelayView rv = (LightRelayView) recyclerView.getChildAt(i);
                if (rv != null) {
                    LightRelayData data = rv.getRelayData();
                    if (data.getId() == id) {
                        rv.setIsOn(data.getState() != 0);
                        break;
                    }
                }
            }
        }
    }

    public void drawRoomSensorState(int id) {
        if (rootView == null)
            return;

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.dashboardRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            if (recyclerView.getChildAt(i) instanceof RoomSensorView) {
                RoomSensorView rv = (RoomSensorView) recyclerView.getChildAt(i);
                if (rv != null) {
                    RoomSensorData data = rv.getSensorData();
                    if (data.getId() == id) {
                        rv.setSensorData(data);
                        break;
                    }
                }
            }
        }
    }

    public void drawBoilerSensorState(int id) {
        if (rootView == null)
            return;

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.dashboardRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            if (recyclerView.getChildAt(i) instanceof BoilerSensorView) {
                BoilerSensorView rv = (BoilerSensorView) recyclerView.getChildAt(i);
                if (rv != null) {
                    BoilerSensorData data = rv.getSensorData();
                    if (data.getId() == id) {
                        rv.setSensorData(data);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getContext().registerReceiver(broadcastDataReceiver, new IntentFilter("ge.altasoft.gia.DASH_CHANGED"));
    }

    @Override
    public void onStop() {
        super.onStop();

        getContext().unregisterReceiver(broadcastDataReceiver);
    }


    final private BroadcastReceiver broadcastDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    };


}
