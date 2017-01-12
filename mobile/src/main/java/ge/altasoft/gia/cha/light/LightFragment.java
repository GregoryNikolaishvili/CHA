package ge.altasoft.gia.cha.light;

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
import ge.altasoft.gia.cha.RelayControllerData;
import ge.altasoft.gia.cha.RelayData;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.DragLinearLayout;
import ge.altasoft.gia.cha.views.LightRelayView;

public class LightFragment extends Fragment implements RelayControllerData.IDrawRelaysUI {

    public DragLinearLayout lightDragLinearLayout = null;
    private View rootView = null;

    public LightFragment() {
    }

    public static LightFragment newInstance() {
        return new LightFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_light, container, false);

        lightDragLinearLayout = (DragLinearLayout) rootView.findViewById(R.id.lightDragLinearLayout);
        lightDragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition,
                               View secondView, int secondPosition) {

                LightControllerData.Instance.reorderRelayMapping(firstPosition, secondPosition);
            }
        });

        ToggleButton tb = ((ToggleButton) rootView.findViewById(R.id.lightsAutoMode));
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (!Utils.disableOnCheckedListener) {
                    ((ToggleButton) button).setTextOn("");
                    ((ToggleButton) button).setTextOff("");
                    button.setEnabled(false);
                    LightUtils.sendCommandToController(isChecked ? "A" : "M");
                }
            }
        });

        return rootView;
    }

    public void setDraggableViews(boolean on) {
        for (int I = 0; I < lightDragLinearLayout.getChildCount(); I++) {
            LinearLayout lt = (LinearLayout) lightDragLinearLayout.getChildAt(I);

            if (on)
                lightDragLinearLayout.setViewDraggable(lt, lt);
            else
                lightDragLinearLayout.setViewNonDraggable(lt);
        }
    }


    public void createNewRelay(final RelayData relayData) {
        Context context = ChaApplication.getAppContext();
        LinearLayout.LayoutParams lpRelay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        LightRelayView relayView = new LightRelayView(context);
        relayView.setLightRelayData((LightRelayData) relayData);
        relayView.setLayoutParams(lpRelay);

        lightDragLinearLayout.addView(relayView);
    }

    public void clearAllRelays() {
        lightDragLinearLayout.removeAllViews();
    }

    public void drawFooterRelays() {
        ToggleButton tvAuto = ((ToggleButton) rootView.findViewById(R.id.lightsAutoMode));
        Utils.disableOnCheckedListener = true;
        try {
            tvAuto.setTextOn(getResources().getString(R.string.auto));
            tvAuto.setTextOff(getResources().getString(R.string.manual));
            tvAuto.setChecked(LightControllerData.Instance.isActive());
            tvAuto.setEnabled(true);
        } finally {
            Utils.disableOnCheckedListener = false;
        }

        ((TextView) rootView.findViewById(R.id.lightsTimeTextView)).setText(LightControllerData.Instance.GetStatusText());
    }
}
