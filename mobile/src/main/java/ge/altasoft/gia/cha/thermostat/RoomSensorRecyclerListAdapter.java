package ge.altasoft.gia.cha.thermostat;

import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.ItemTouchHelperAdapter;
import ge.altasoft.gia.cha.classes.ItemTouchHelperViewHolder;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.views.RoomSensorView;

public class RoomSensorRecyclerListAdapter extends RecyclerView.Adapter<RoomSensorRecyclerListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;

    public RoomSensorRecyclerListAdapter(OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = new RoomSensorView(parent.getContext());

        //int height = parent.getMeasuredWidth() / 4;
        int height = parent.getMeasuredHeight() / 4;
        itemView.setMinimumHeight(height);

        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {

        RoomSensorData data = ThermostatControllerData.Instance.getRoomSensorFromUIIndex(position);
        ((RoomSensorView) holder.itemView).setSensorData((RoomSensorData) data);

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
        ThermostatControllerData.Instance.reorderRoomSensorMapping(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return ThermostatControllerData.Instance.roomSensorCount();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private final View handleView;

        ItemViewHolder(View itemView) {
            super(itemView);
            handleView = itemView.findViewById(R.id.sensor_layout);
        }

        @Override
        public void onItemSelected() {
            ((CardView) ((ViewGroup) itemView).getChildAt(0)).setCardBackgroundColor(Color.GRAY);
        }

        @Override
        public void onItemClear() {
            ((CardView) ((ViewGroup) itemView).getChildAt(0)).setCardBackgroundColor(Color.WHITE);
        }
    }
}
