package ge.altasoft.gia.cha;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.DashboardItem;
import ge.altasoft.gia.cha.classes.DashboardItems;
import ge.altasoft.gia.cha.classes.ItemTouchHelperAdapter;
import ge.altasoft.gia.cha.classes.ItemViewHolder;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.light.LightRelayData;
import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.views.BoilerSensorView;
import ge.altasoft.gia.cha.views.LightRelayView;
import ge.altasoft.gia.cha.views.OutsideSensorView;
import ge.altasoft.gia.cha.views.PressureSensorView;
import ge.altasoft.gia.cha.views.RainSensorView;
import ge.altasoft.gia.cha.views.RoomSensorView;
import ge.altasoft.gia.cha.views.WindSensorView;

class DashboardRecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;

    DashboardRecyclerListAdapter(OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = new FrameLayout(parent.getContext());

        int height = parent.getMeasuredHeight() / 4;
        itemView.setMinimumHeight(height);

        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {

        ViewGroup ll = (ViewGroup) holder.itemView;
        if (ll.getChildCount() > 0)
            ll.removeAllViews();

        ChaWidget w;

        DashboardItem item = DashboardItems.getItemAt(position);
        switch (item.type) {
            case LightRelay:
                RelayData rd = LightControllerData.Instance.relays(item.id);
                w = new LightRelayView(ll.getContext(), true);
                ((LightRelayView) w).setRelayData((LightRelayData) rd);
                break;

            case RoomSensor:
                RoomSensorData rsd = ThermostatControllerData.Instance.roomSensors(item.id, false);
                w = new RoomSensorView(ll.getContext(), true);
                ((RoomSensorView) w).setSensorData(rsd);
                break;

            case BoilerSensor:
                BoilerSensorData sd = ThermostatControllerData.Instance.boilerSensors(item.id);
                w = new BoilerSensorView(ll.getContext(), true);
                ((BoilerSensorView) w).setSensorData(sd);
                break;

            case OutsideSensor:
                w = new OutsideSensorView(ll.getContext(), true);
                w.refresh();
                break;
            case WindSensor:
                w = new WindSensorView(ll.getContext(), true);
                w.refresh();
                break;
            case PressureSensor:
                w = new PressureSensorView(ll.getContext(), true);
                w.refresh();
                break;
            case RainSensor:
                w = new RainSensorView(ll.getContext(), true);
                w.refresh();
                break;

            default:
                return;
        }

        int height = ViewCompat.getMinimumHeight(ll);
        w.setMinimumHeight(height);
        w.setLayoutParams(new FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        ll.addView(w);
        View handleView = w.findViewById(R.id.main_layout);

        //Start a drag whenever the handle view it touched
        handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public void onItemDismiss(int position) {
        //mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        DashboardItems.setWidgetOrderChanged(true);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return DashboardItems.size();
    }
}
