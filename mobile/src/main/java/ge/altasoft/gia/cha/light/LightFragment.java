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

import ge.altasoft.gia.cha.MainActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.DragLinearLayout;
import ge.altasoft.gia.cha.views.LightRelayView;

public class LightFragment extends Fragment {

    private DragLinearLayout dragLinearLayout = null;
    private View rootView = null;

    public LightFragment() {
    }

    public static LightFragment newInstance() {
        return new LightFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_light, container, false);

        dragLinearLayout = (DragLinearLayout) rootView.findViewById(R.id.lightDragLinearLayout);
        dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
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
                    //LightUtils.sendCommandToController(getContext(), isChecked ? "A" : "M");
                    ((MainActivity) getActivity()).getMqttClient().publish("chac/light/mode", isChecked ? "A" : "M", false);
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

    // rebuild everything and draw new state
    public void rebuildUI() {
        if (!LightControllerData.Instance.haveSettings() || (rootView == null))
            return;

        View vLoading = dragLinearLayout.findViewById(R.id.lightLoading);
        if (vLoading != null)
            dragLinearLayout.removeView(vLoading);

        dragLinearLayout.removeAllViews();

        Context context = getContext();
        LinearLayout.LayoutParams lpRelay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        RelayData[] relays = LightControllerData.Instance.sortedRelays();
        for (RelayData data : relays) {
            LightRelayView relayView = new LightRelayView(context);
            relayView.setRelayData((LightRelayData) data);
            relayView.setLayoutParams(lpRelay);

            dragLinearLayout.addView(relayView);
        }

        drawFooter();
    }

    public void drawState() {

        if (rootView == null)
            return;

        for (int i = 0; i < dragLinearLayout.getChildCount(); i++) {
            if (dragLinearLayout.getChildAt(i) instanceof LightRelayView) {
                LightRelayView rv = (LightRelayView) dragLinearLayout.getChildAt(i);
                LightRelayData data = rv.getRelayData();
                rv.setIsOn(data.isOn());
            }
        }

        drawFooter();
    }

    public void drawState(int id) {

        if (rootView == null)
            return;

        for (int i = 0; i < dragLinearLayout.getChildCount(); i++) {
            if (dragLinearLayout.getChildAt(i) instanceof LightRelayView) {
                LightRelayView rv = (LightRelayView) dragLinearLayout.getChildAt(i);
                LightRelayData data = rv.getRelayData();
                if (data.getId() == id) {
                    rv.setIsOn(data.isOn());
                    break;
                }
            }
        }
    }

    private void drawFooter() {
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
