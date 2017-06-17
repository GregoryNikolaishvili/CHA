package ge.altasoft.gia.cha.classes;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;

public class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

    public final ViewGroup mainLayout;

    public ItemViewHolder(View itemView) {
        super(itemView);
        mainLayout = (ViewGroup) itemView.findViewById(R.id.main_layout);
    }

    private CardView getWidgetCard() {
        View v = ((ViewGroup) itemView).getChildAt(0);
        if (v instanceof CardView)
            return (CardView) v;

        if (v instanceof ViewGroup)
            v = ((ViewGroup) v).getChildAt(0);

        if (v instanceof CardView)
            return (CardView) v;
        else
            return null;
    }

    @Override
    public void onItemSelected() {
        CardView v = getWidgetCard();
        if (v != null)
            v.setCardBackgroundColor(Utils.getCardBackgroundColor(true, false));
    }

    @Override
    public void onItemClear() {
        CardView v = getWidgetCard();
        if (v != null)
            v.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));
    }
}