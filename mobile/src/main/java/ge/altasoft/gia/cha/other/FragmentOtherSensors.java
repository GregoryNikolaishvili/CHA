package ge.altasoft.gia.cha.other;

import android.support.v7.widget.RecyclerView;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.ChaFragment;
import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.ItemViewHolder;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.WidgetType;

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
                ChaWidget w = getWidgetAt(recyclerView, i);
                if (w != null)
                    w.refresh();
            }
        }
    }

    @Override
    public void saveWidgetOrders() {
    }


    @Override
    public void rebuildUI(boolean isStart) {
        if ((rootView == null) || (OtherControllerData.Instance == null)) // || !OtherControllerData.Instance.haveSettings())
            return;

        hideWaitingScreen();

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ChaWidget w = getWidgetAt(recyclerView, i);
            if (w != null)
                w.refresh();
        }
    }

    public void drawState(WidgetType wt, int widgetId) {
        if (rootView == null)
            return;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ChaWidget w = getWidgetAt(recyclerView, i);
            if (w != null) {
                if ((w.getWidgetId() == widgetId) && (w.getWidgetType() == wt)) {
                    w.refresh();
                    break;
                }
            }
        }
    }
}