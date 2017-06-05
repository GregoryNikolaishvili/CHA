package ge.altasoft.gia.cha.light;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import ge.altasoft.gia.cha.ChaActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.ChaFragment;
import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.ItemViewHolder;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.views.LightRelayView;

public class FragmentLight extends ChaFragment implements OnStartDragListener {

    public FragmentLight() {
    }

    @Override
    protected boolean canReorder() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_light;
    }

    @Override
    protected RecyclerView.Adapter<ItemViewHolder> getRecycleAdapter() {
        return new LightRecyclerListAdapter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final CardView cv = ((CardView) rootView.findViewById(R.id.lightsAutoMode));

        cv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        ((TextView) cv.getChildAt(0)).setText("⌛");
                        cv.setEnabled(false);

                        switch (item.getItemId()) {
                            case R.id.item_auto:
                                ((ChaActivity) getActivity()).publish("chac/lc/mode", "A", false);
                                break;
                            case R.id.item_manual:
                                ((ChaActivity) getActivity()).publish("chac/lc/mode", "M", false);
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.light_mode_popup_menu);
                popupMenu.show();

                return true;
            }
        });

        return rootView;
    }


    @Override
    public void rebuildUI(boolean isStart) {
        if ((rootView == null) || (LightControllerData.Instance == null) || !LightControllerData.Instance.haveSettings())
            return;

        hideWaitingScreen();

        RelayData[] relays = LightControllerData.Instance.sortedRelays();
        for (int i = 0; i < relays.length; i++) {
            RelayData data = relays[i];
            LightRelayView rv = (LightRelayView) recyclerView.getChildAt(i);
            if (rv != null)
                rv.setRelayData((LightRelayData) data);
        }

        drawFooter();
    }

    @Override
    public void saveWidgetOrders() {
        if (LightControllerData.Instance.widgetOrderChanged()) {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                ChaWidget w = getWidgetAt(recyclerView, i);
                if (w != null)
                    LightControllerData.Instance.relays(w.getWidgetId()).setOrder(i);
            }

            ((ChaActivity) getActivity()).publish("chac/lc/settings/names", LightControllerData.Instance.encodeNamesAndOrder(), false);

            LightControllerData.Instance.setWidgetOrderChanged(false);
        }
    }

    @Override
    public void checkSensors() {
    }

    public void drawState(int id) {
        if (rootView == null)
            return;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            LightRelayView rv = (LightRelayView) recyclerView.getChildAt(i);
            if (rv.getWidgetId() == id) {
                rv.refresh();
                break;
            }
        }
    }

    private void drawFooter() {
        CardView cv = ((CardView) rootView.findViewById(R.id.lightsAutoMode));
        ((TextView) cv.getChildAt(0)).setText(LightControllerData.Instance.isActive() ? "Auto" : "Manual");
    }
}