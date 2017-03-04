package ge.altasoft.gia.cha.other;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.ChaFragment;
import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.ItemViewHolder;
import ge.altasoft.gia.cha.classes.OnStartDragListener;

public class FragmentOtherSensors extends ChaFragment implements OnStartDragListener {

    public FragmentOtherSensors() {
    }

    @Override
    protected boolean canReorder() {
        return false;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_other_sensors;
    }

    @Override
    protected RecyclerView.Adapter<ItemViewHolder> getRecycleAdapter() {
        return new OtherSensorRecyclerListAdapter();
    }

    @Override
    public void checkSensors() {
        if (rootView != null) {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                ChaWidget w = (ChaWidget) getViewAt(recyclerView, i);
                if (w != null)
                    w.refresh();
            }
        }
    }

    @Override
    public void rebuildUI() {
        if ((rootView == null) || (OtherControllerData.Instance == null))
            return;

        hideWaitingScreen();

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ChaWidget w = (ChaWidget) getViewAt(recyclerView, i);
            if (w != null)
                w.refresh();
        }
    }

    public void drawState(int id) {
        if (rootView == null)
            return;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ChaWidget w = (ChaWidget) getViewAt(recyclerView, i);
            if ((w != null) && (w.getWidgetId() == id)) {
                w.refresh();
                break;
            }
        }
    }

    private View getViewAt(RecyclerView recyclerView, int position) {
        View v = recyclerView.getChildAt(position);
        if (v instanceof ViewGroup)
            return ((ViewGroup) v).getChildAt(0);
        else
            return null;
    }
}