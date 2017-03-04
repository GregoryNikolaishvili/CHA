package ge.altasoft.gia.cha.other;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.ItemTouchHelperAdapter;
import ge.altasoft.gia.cha.classes.ItemViewHolder;

public class OtherSensorRecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> implements ItemTouchHelperAdapter {

    public OtherSensorRecyclerListAdapter() {
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

        ChaWidget w = OtherControllerData.Instance.createWidget(ll.getContext(), position, false);
        if (w != null) {
            int height = ViewCompat.getMinimumHeight(ll);
            w.setMinimumHeight(height);
            w.setLayoutParams(new FrameLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));
            ll.addView(w);
        }
    }

    @Override
    public void onItemDismiss(int position) {
        //mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return OtherControllerData.Instance.sensorCount();
    }

}
