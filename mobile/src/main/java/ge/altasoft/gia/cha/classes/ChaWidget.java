package ge.altasoft.gia.cha.classes;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

import ge.altasoft.gia.cha.LogActivity2;
import ge.altasoft.gia.cha.LogActivityTH2;
import ge.altasoft.gia.cha.LogStateActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;

public abstract class ChaWidget extends LinearLayout {
    private boolean dragMode = false;
    private boolean fromDashboard = false;
    private boolean isPressed = false;

    protected CardView cardView = null;

    protected ChaWidget(Context context, boolean fromDashboard) {
        this(context, null);
        this.fromDashboard = fromDashboard;
    }

    public ChaWidget(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChaWidget(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDragMode(boolean dragMode) {
        this.dragMode = dragMode;
    }

    private boolean getDragMode() {
        return this.dragMode;
    }

    protected boolean getIsFromDashboard() {
        return this.fromDashboard;
    }


    protected abstract boolean canClick();

    protected abstract void onClick();

    public abstract WidgetType getWidgetType();

    public abstract int getWidgetId();

    protected abstract int getPopupMenuResId();

    public abstract void refresh();

    protected abstract long getLastSyncTime();

    protected void afterInflate() {
        setTag(false);

        cardView = (CardView) getChildAt(0);

        if (canClick())
            setTouchListener();
        else
            setLongClickListener();
    }

    private void setLongClickListener() {
        cardView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(true, false));
                PopupMenu popupMenu = new PopupMenu(getContext(), v);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        menuItemClick(item);
                        return false;
                    }
                });
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));
                    }
                });

                popupMenu.inflate(getPopupMenuResId());
                if (popupMenu.getMenu().findItem(R.id.item_pin_to_dashboard) != null)
                    popupMenu.getMenu().findItem(R.id.item_pin_to_dashboard).setChecked(DashboardItems.hasItem(getWidgetType(), getWidgetId()));

                if (popupMenu.getMenu().findItem(R.id.item_log_last_sync) != null)
                    popupMenu.getMenu().findItem(R.id.item_log_last_sync).setTitle(DateUtils.getRelativeTimeSpanString(getLastSyncTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));

                popupMenu.show();

                return true;
            }
        });
    }

    private void setTouchListener() {
        cardView.setOnTouchListener(new OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View v, MotionEvent event) {
                                            if (!getDragMode()) {
                                                switch (event.getAction()) {
                                                    case MotionEvent.ACTION_DOWN:
                                                        setIsPressed(true);
                                                        longPressHandle.postDelayed(longPressCall, ViewConfiguration.getLongPressTimeout());
                                                        break;

                                                    case MotionEvent.ACTION_CANCEL:
                                                        longPressHandle.removeCallbacks(longPressCall);
                                                        setIsPressed(false);
                                                        break;

                                                    case MotionEvent.ACTION_UP:
                                                        longPressHandle.removeCallbacks(longPressCall);
                                                        if (!(boolean) getTag()) {
                                                            setIsPressed(false);
                                                            onClick();
                                                        }
                                                }
                                                //Log.d("Touch", actionToString(event.getAction()));
                                                return true;
                                            }
                                            return false;
                                        }
                                    }
        );
    }

    private void setIsPressed(boolean pressed) {
        this.isPressed = pressed;
        cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(pressed, false));
    }

    final private Handler longPressHandle = new Handler();

    final private Runnable longPressCall = new Runnable() {
        @Override
        public void run() {
            if (isPressed)
                onLongPress();
        }
    };

    private void onLongPress() {
        cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(true, false));
        PopupMenu popupMenu = new PopupMenu(getContext(), cardView);
        setTag(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setTag(false);
                menuItemClick(item);
                return false;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                setTag(false);
                cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));
            }
        });
        popupMenu.inflate(getPopupMenuResId());
        if (popupMenu.getMenu().findItem(R.id.item_pin_to_dashboard) != null)
            popupMenu.getMenu().findItem(R.id.item_pin_to_dashboard).setChecked(DashboardItems.hasItem(getWidgetType(), getWidgetId()));
        popupMenu.show();
    }

    protected void menuItemClick(MenuItem item) {
        setTag(false);
        switch (item.getItemId()) {
            case R.id.item_pin_to_dashboard:
                if (DashboardItems.hasItem(getWidgetType(), getWidgetId()))
                    DashboardItems.remove(getContext(), getWidgetType(), getWidgetId());
                else
                    DashboardItems.add(getContext(), getWidgetType(), getWidgetId());
                break;

            case R.id.item_log_and_chart: {
                Intent intent = new Intent(getContext(), LogActivityTH2.class);
                intent.putExtra("id", getWidgetId());
                intent.putExtra("widget", getWidgetType());
                getContext().startActivity(intent);
                break;
            }

            case R.id.item_log_and_chart2: {
                //Intent intent = new Intent(getContext(), Log5in1Activity.class);
                Intent intent = new Intent(getContext(), LogActivity2.class);
                intent.putExtra("id", getWidgetId());
                intent.putExtra("widget", getWidgetType());
                getContext().startActivity(intent);
                break;
            }

            case R.id.item_log: {
                Intent intent = new Intent(getContext(), LogStateActivity.class);
                intent.putExtra("id", getWidgetId());
                intent.putExtra("widget", getWidgetType());
                getContext().startActivity(intent);
                break;
            }
        }
    }
}
