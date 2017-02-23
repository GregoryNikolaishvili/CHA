package ge.altasoft.gia.cha.light;

import android.graphics.Color;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.ItemTouchHelperAdapter;
import ge.altasoft.gia.cha.classes.ItemTouchHelperViewHolder;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.views.LightRelayView;

public class LightRecyclerListAdapter extends RecyclerView.Adapter<LightRecyclerListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private final OnStartDragListener mDragStartListener;

    public LightRecyclerListAdapter(OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = new LightRelayView(parent.getContext(), false);

        //int height = parent.getMeasuredWidth() / 4;
        int height = parent.getMeasuredHeight() / 4;
        itemView.setMinimumHeight(height);

        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {

        RelayData data = LightControllerData.Instance.getRelayFromUIIndex(position);
        ((LightRelayView) holder.itemView).setRelayData((LightRelayData) data);

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
        LightControllerData.Instance.reorderRelayMapping(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return LightControllerData.Instance.relayCount();
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
