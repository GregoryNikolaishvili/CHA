package ge.altasoft.gia.cha.light;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import ge.altasoft.gia.cha.ChaActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.RecyclerListAdapter;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.SimpleItemTouchHelperCallback;
import ge.altasoft.gia.cha.views.LightRelayView;

public class FragmentLight extends Fragment implements OnStartDragListener {

    //private DragLinearLayout dragLinearLayout = null;
    private ViewGroup rootView = null;
    private boolean dragMode = false;
    private TextView tvLoading;

    public FragmentLight() {
    }

    public static FragmentLight newInstance() {
        return new FragmentLight();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_light, container, false);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tvLoading = new TextView(getContext());

        tvLoading.setLayoutParams(lp);
        tvLoading.setGravity(Gravity.CENTER);
        tvLoading.setText(getResources().getString(R.string.loading));
        rootView.addView(tvLoading, 0);

//        dragLinearLayout = (DragLinearLayout) rootView.findViewById(R.id.lightDragLinearLayout);
//        dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
//            @Override
//            public void onSwap(View firstView, int firstPosition,
//                               View secondView, int secondPosition) {
//
//                LightControllerData.Instance.reorderRelayMapping(firstPosition, secondPosition);
//            }
//        });

        ToggleButton tb = ((ToggleButton) rootView.findViewById(R.id.lightsAutoMode));
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (!Utils.disableOnCheckedListener) {
                    ((ToggleButton) button).setTextOn("");
                    ((ToggleButton) button).setTextOff("");
                    button.setEnabled(false);
                    ((ChaActivity) getActivity()).publish("chac/light/mode", isChecked ? "A" : "M", false);
                }
            }
        });

        rebuildUI();

        return rootView;
    }

    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerListAdapter adapter = new RecyclerListAdapter(this);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.lightRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        //recyclerView.addItemDecoration(new SpacesItemDecoration(8));

        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        if (dragMode)
            mItemTouchHelper.startDrag(viewHolder);
    }

    public void setDraggableViews(boolean on) {
        dragMode = on;
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.lightRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            LightRelayView relayView = (LightRelayView) recyclerView.getChildAt(i);
            relayView.setDragMode(on);
        }

//        for (int I = 0; I < dragLinearLayout.getChildCount(); I++) {
//            if (dragLinearLayout.getChildAt(I) instanceof LinearLayout) {
//                LinearLayout lt = (LinearLayout) dragLinearLayout.getChildAt(I);
//
//                if (on)
//                    dragLinearLayout.setViewDraggable(lt, lt);
//                else
//                    dragLinearLayout.setViewNonDraggable(lt);
//            }
//        }
    }

    // rebuild everything and draw new state
    public void rebuildUI() {
        if ((rootView == null) || (LightControllerData.Instance == null) || !LightControllerData.Instance.haveSettings())
            return;

        if (tvLoading != null)
            rootView.removeView(tvLoading);

//        View vLoading = dragLinearLayout.findViewById(R.id.lightLoading);
//        if (vLoading != null)
//            dragLinearLayout.removeView(vLoading);
//
//        dragLinearLayout.removeAllViews();

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.lightRecyclerView);

        RelayData[] relays = LightControllerData.Instance.sortedRelays();
        for (int i = 0; i < relays.length; i++) {
            RelayData data = relays[i];
            LightRelayView relayView = (LightRelayView) recyclerView.getChildAt(i);
            if (relayView != null)
                relayView.setRelayData((LightRelayData) data);
        }

        drawFooter();
    }

//    public void drawState() {
//
//        if (rootView == null)
//            return;
//
//        for (int i = 0; i < dragLinearLayout.getChildCount(); i++) {
//            if (dragLinearLayout.getChildAt(i) instanceof LightRelayView) {
//                LightRelayView rv = (LightRelayView) dragLinearLayout.getChildAt(i);
//                LightRelayData data = rv.getRelayData();
//                rv.setIsOn(data.isOn());
//            }
//        }
//
//        drawFooter();
//    }

    public void drawState(int id) {
        if (rootView == null)
            return;

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.lightRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            LightRelayView rv = (LightRelayView) recyclerView.getChildAt(i);
            LightRelayData data = rv.getRelayData();
            if (data.getId() == id) {
                rv.setIsOn(data.isOn());
                break;
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

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            outRect.left = space;
            outRect.top = space;

//            // Add top margin only for the left item to avoid double space between items
//            if (parent.getChildLayoutPosition(view) % 4 == 0) {
//                outRect.left = space;
//            } else {
//                outRect.left = 0;
//            }
//            // Add top margin only for the first item to avoid double space between items
//            if (parent.getChildLayoutPosition(view) < 4) {
//                outRect.top = space;
//            } else {
//                outRect.top = 0;
//            }
        }
    }
}
