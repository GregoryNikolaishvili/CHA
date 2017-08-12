package ge.altasoft.gia.cha;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ge.altasoft.gia.cha.classes.ChaFragment;
import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.DashboardItems;
import ge.altasoft.gia.cha.classes.ItemViewHolder;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.light.LightControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class FragmentDashboard extends ChaFragment implements OnStartDragListener {

    private final Map<String, String> controllerStates = new HashMap<>();

    public FragmentDashboard() {
    }

    @Override
    protected boolean canReorder() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_dashboard;
    }

    @Override
    protected RecyclerView.Adapter<ItemViewHolder> getRecycleAdapter() {
        return new DashboardRecyclerListAdapter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        DashboardItems.restoreFromPreferences(getContext());

        return rootView;
    }

    @Override
    public void rebuildUI(boolean isStart) {
        if (rootView == null) return;

        if ((LightControllerData.Instance == null) || !LightControllerData.Instance.haveSettings())
            return;
        if ((ThermostatControllerData.Instance == null) || !ThermostatControllerData.Instance.haveBoilerSettings())
            return;

        hideWaitingScreen();

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ChaWidget w = getWidgetAt(recyclerView, i);
            if (w != null)
                w.refresh();
        }

        //drawFooter();
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

//    private void drawFooter() {
//    }

    public void drawWidgetState(WidgetType wt, int widgetId) {
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

    public void drawAllWidgetStates(WidgetType wt) {
        if (rootView == null)
            return;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            ChaWidget w = getWidgetAt(recyclerView, i);
            if (w != null) {
                if (w.getWidgetType() == wt)
                    w.refresh();
            }
        }
    }

    @Override
    public void saveWidgetOrders() {
        if (DashboardItems.widgetOrderChanged()) {
            DashboardItems.clear();
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                ChaWidget w = getWidgetAt(recyclerView, i);
                if (w != null)
                    DashboardItems.add(w.getWidgetType(), w.getWidgetId());
            }

            DashboardItems.saveToPreferences(getActivity());

            DashboardItems.setWidgetOrderChanged(false);
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

    public void drawControllersState(String scope, StringBuilder sb) {
        if (rootView == null)
            return;

        if ((sb == null) || (sb.length() == 0))
            controllerStates.put(scope, "");
        else {
            String msg = sb.toString().replace('\r', ',').replace('\n', ' ');
            controllerStates.put(scope, msg);
        }

        StringBuilder msgBuilder = new StringBuilder();

        for (Map.Entry<String, String> pairs : controllerStates.entrySet()) {
            scope = pairs.getKey();
            String value = pairs.getValue();
            if ((value != null) && (value.length() > 0)) {
                msgBuilder.append(scope);
                msgBuilder.append(": ");
                msgBuilder.append(value);

                msgBuilder.append("\n");
            }
        }

        if (msgBuilder.length() >= 1) // delete last "\n"
            msgBuilder.setLength(msgBuilder.length() - 1);

        ((TextView) rootView.findViewById(R.id.controllersStatus)).setText(msgBuilder.toString());
    }


}
