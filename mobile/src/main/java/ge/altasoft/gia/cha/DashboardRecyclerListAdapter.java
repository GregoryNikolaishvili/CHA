package ge.altasoft.gia.cha;

import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import ge.altasoft.gia.cha.classes.DashboardItem;
import ge.altasoft.gia.cha.classes.DashboardItems;
import ge.altasoft.gia.cha.classes.ItemTouchHelperAdapter;
import ge.altasoft.gia.cha.classes.ItemTouchHelperViewHolder;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.light.LightRelayData;
import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.views.BoilerSensorView;
import ge.altasoft.gia.cha.views.LightRelayView;
import ge.altasoft.gia.cha.views.RoomSensorView;

public class DashboardRecyclerListAdapter extends RecyclerView.Adapter<DashboardRecyclerListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;

    public DashboardRecyclerListAdapter(OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;
    }

    @Override
    public int getItemViewType(int position) {
        DashboardItem item = DashboardItems.getItemAt(position);
        return item.type;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        switch (viewType) {
            case 0:
                itemView = new LightRelayView(parent.getContext(), true);
                break;
            case 1:
                itemView = new RoomSensorView(parent.getContext(), true);
                break;
            case 2:
                itemView = new BoilerSensorView(parent.getContext(), true);
                break;
            default:
                return null;
        }

        //int height = parent.getMeasuredWidth() / 4;
        int height = parent.getMeasuredHeight() / 4;
        itemView.setMinimumHeight(height);

        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {

        DashboardItem item = DashboardItems.getItemAt(position);
        switch (item.type) {
            case 0: {
                RelayData data = LightControllerData.Instance.relays(item.id);
                ((LightRelayView) holder.itemView).setRelayData((LightRelayData) data);
            }
            break;

            case 1: {
                RoomSensorData data = ThermostatControllerData.Instance.roomSensors(item.id, false);
                ((RoomSensorView) holder.itemView).setSensorData(data);
            }
            break;

            case 2: {
                BoilerSensorData data = ThermostatControllerData.Instance.boilerSensors(item.id);
                ((BoilerSensorView) holder.itemView).setSensorData(data);
            }
            break;

            default:
                return;
        }

        //Start a drag whenever the handle view it touched
        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
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
        //LightControllerData.Instance.reorderRelayMapping(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return DashboardItems.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private final View handleView;

        ItemViewHolder(View itemView) {
            super(itemView);
            handleView = itemView.findViewById(R.id.main_layout);
        }

        @Override
        public void onItemSelected() {
            ((CardView) ((ViewGroup) itemView).getChildAt(0)).setCardBackgroundColor(Utils.getCardBackgroundColor(itemView.getContext(), true, false));
        }

        @Override
        public void onItemClear() {
            ((CardView) ((ViewGroup) itemView).getChildAt(0)).setCardBackgroundColor(Utils.getCardBackgroundColor(itemView.getContext(), false, false));
        }
    }
}
