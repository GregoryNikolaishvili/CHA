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

import ge.altasoft.gia.cha.ChaApplication;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.RelayData;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.DragLinearLayout;
import ge.altasoft.gia.cha.views.ThermostatRelayView;

public class ThermostatRelaysFragment extends Fragment {

    public DragLinearLayout thermostatRelaysDragLinearLayout = null;
    private View rootView = null;

    public ThermostatRelaysFragment() {
    }

    public static ThermostatRelaysFragment newInstance() {
        return new ThermostatRelaysFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_thermostat_relays, container, false);

        thermostatRelaysDragLinearLayout = (DragLinearLayout) rootView.findViewById(R.id.thermostatRelayDragLinearLayout);
        thermostatRelaysDragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
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
                    ThermostatUtils.sendCommandToController(isChecked ? "A" : "M");
                }
            }
        });

        return rootView;
    }

    public void setDraggableViews(boolean on) {
        for (int I = 0; I < thermostatRelaysDragLinearLayout.getChildCount(); I++) {
            LinearLayout lt = (LinearLayout) thermostatRelaysDragLinearLayout.getChildAt(I);

            if (on)
                thermostatRelaysDragLinearLayout.setViewDraggable(lt, lt);
            else
                thermostatRelaysDragLinearLayout.setViewNonDraggable(lt);
        }
    }

    public void createNewRelay(RelayData relayData) {
        Context context = ChaApplication.getAppContext();
        LinearLayout.LayoutParams lpRelay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        ThermostatRelayView relay = new ThermostatRelayView(context);
        relay.setThermostatRelayData((ThermostatRelayData) relayData);
        relay.setLayoutParams(lpRelay);

        thermostatRelaysDragLinearLayout.addView(relay);
    }

    public void clearAllRelays() {
        thermostatRelaysDragLinearLayout.removeAllViews();
    }

    public void drawFooterRelays() {
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