package ge.altasoft.gia.cha.light;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ge.altasoft.gia.cha.ChaActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.classes.ItemTouchHelperAdapter;
import ge.altasoft.gia.cha.classes.OnStartDragListener;
import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.classes.SimpleItemTouchHelperCallback;
import ge.altasoft.gia.cha.views.LightRelayView;

public class FragmentLight extends Fragment implements OnStartDragListener {

    private ViewGroup rootView = null;
    private boolean dragMode = false;
    private TextView tvLoading;
    private ItemTouchHelper mItemTouchHelper;

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
                                ((ChaActivity) getActivity()).publish("chac/light/mode", "A", false);
                                break;
                            case R.id.item_manual:
                                ((ChaActivity) getActivity()).publish("chac/light/mode", "M", false);
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

        rebuildUI();

        return rootView;
    }

    private class MySimpleItemTouchHelperCallback extends SimpleItemTouchHelperCallback {
        public MySimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            super(adapter);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return dragMode;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LightRecyclerListAdapter adapter = new LightRecyclerListAdapter(this);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.lightRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(layoutManager);

        ItemTouchHelper.Callback callback = new MySimpleItemTouchHelperCallback(adapter);
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
            LightRelayView view = (LightRelayView) recyclerView.getChildAt(i);
            view.setDragMode(on);
        }
    }

    // rebuild everything and draw new state
    public void rebuildUI() {
        if ((rootView == null) || (LightControllerData.Instance == null) || !LightControllerData.Instance.haveSettings())
            return;

        if (tvLoading != null) {
            rootView.removeView(tvLoading);
            tvLoading = null;
        }

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
//        drawFooter();
//    }

    public void drawState(int id) {
        if (rootView == null)
            return;

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.lightRecyclerView);
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            LightRelayView rv = (LightRelayView) recyclerView.getChildAt(i);
            if (rv != null) {
                LightRelayData data = rv.getRelayData();
                if (data.getId() == id) {
                    rv.setIsOn(data.getState() != 0);
                    break;
                }
            }
        }
    }

    private void drawFooter() {
        CardView cv = ((CardView) rootView.findViewById(R.id.lightsAutoMode));
        ((TextView) cv.getChildAt(0)).setText(LightControllerData.Instance.isActive() ? "Auto" : "Manual");
    }

//    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
//        private int space;
//
//        public SpacesItemDecoration(int space) {
//            this.space = space;
//        }
//
//        @Override
//        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//            outRect.left = space;
//            outRect.right = space;
//            outRect.bottom = space;
//
//            outRect.left = space;
//            outRect.top = space;
//
////            // Add top margin only for the left item to avoid double space between items
////            if (parent.getChildLayoutPosition(view) % 4 == 0) {
////                outRect.left = space;
////            } else {
////                outRect.left = 0;
////            }
////            // Add top margin only for the first item to avoid double space between items
////            if (parent.getChildLayoutPosition(view) < 4) {
////                outRect.top = space;
////            } else {
////                outRect.top = 0;
////            }
//        }
//    }
}
