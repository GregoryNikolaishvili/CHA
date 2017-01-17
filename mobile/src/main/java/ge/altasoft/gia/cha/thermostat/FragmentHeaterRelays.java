package ge.altasoft.gia.cha.thermostat;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.DragLinearLayout;
import ge.altasoft.gia.cha.views.ThermostatRelayView;

public class FragmentHeaterRelays extends Fragment {

    private View rootView = null;
    private DragLinearLayout dragLinearLayout = null;

    public FragmentHeaterRelays() {
    }

    public static FragmentHeaterRelays newInstance() {
        return new FragmentHeaterRelays();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_thermostat_relays, container, false);

        dragLinearLayout = (DragLinearLayout) rootView.findViewById(R.id.thermostatRelayDragLinearLayout);
        dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition,
                               View secondView, int secondPosition) {

                ThermostatControllerData.Instance.reorderRoomSensorMapping(firstPosition, secondPosition);
            }
        });

        ToggleButton tb = ((ToggleButton) rootView.findViewById(R.id.thermostatAutoMode));
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (!Utils.disableOnCheckedListener) {
                    ((ToggleButton) button).setTextOn("");
                    ((ToggleButton) button).setTextOff("");
                    button.setEnabled(false);
                    ThermostatUtils.sendCommandToController(getContext(), isChecked ? "A" : "M");
                }
            }
        });

        rebuildUI();

        return rootView;
    }

    public void setDraggableViews(boolean on) {
        for (int I = 0; I < dragLinearLayout.getChildCount(); I++) {
            LinearLayout lt = (LinearLayout) dragLinearLayout.getChildAt(I);

            if (on)
                dragLinearLayout.setViewDraggable(lt, lt);
            else
                dragLinearLayout.setViewNonDraggable(lt);
        }
    }

    // rebuild everything and draws new state
    public void rebuildUI() {
        if (!ThermostatControllerData.Instance.haveSettings() || (rootView == null))
            return;

        View vLoading = dragLinearLayout.findViewById(R.id.roomRelaysLoading);
        if (vLoading != null)
            dragLinearLayout.removeView(vLoading);

        dragLinearLayout.removeAllViews();

        Context context = getContext();
        LinearLayout.LayoutParams lpRelay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        RelayData[] relays = ThermostatControllerData.Instance.sortedRelays();
        for (RelayData data : relays) {
            ThermostatRelayView relayView = new ThermostatRelayView(context);
            relayView.setRelayData((ThermostatRelayData) data);
            relayView.setLayoutParams(lpRelay);

            dragLinearLayout.addView(relayView);
        }

        drawFooter();
    }

    public void drawState() {
        if (rootView == null)
            return;

        for (int i = 0; i < dragLinearLayout.getChildCount(); i++) {
            if (dragLinearLayout.getChildAt(i) instanceof ThermostatRelayView) {
                ThermostatRelayView rv = (ThermostatRelayView) dragLinearLayout.getChildAt(i);
                ThermostatRelayData data = rv.getRelayData();
                rv.setIsOn(data.isOn());
            }
        }

        drawFooter();
    }

    private void drawFooter() {
        ToggleButton tvAuto = ((ToggleButton) rootView.findViewById(R.id.thermostatAutoMode));
        Utils.disableOnCheckedListener = true;
        try {
            tvAuto.setTextOn(getResources().getString(R.string.active));
            tvAuto.setTextOff(getResources().getString(R.string.off));
            tvAuto.setChecked(ThermostatControllerData.Instance.isActive());
            tvAuto.setEnabled(true);
        } finally {
            Utils.disableOnCheckedListener = false;
        }

        ((TextView) rootView.findViewById(R.id.thermostatRelaysTimeTextView)).setText(ThermostatControllerData.Instance.GetStatusText());
    }
}
