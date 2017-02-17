package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import ge.altasoft.gia.cha.ChaActivity;
import ge.altasoft.gia.cha.LogBooleanActivity;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.light.LightRelayData;
import ge.altasoft.gia.cha.R;

public class LightRelayView extends LinearLayout {

    private enum ButtonState {UNKNOWN, ON, OFF, WAIT}

    private boolean isPressed = false;
    private boolean dragMode = false;
    private ButtonState buttonState = ButtonState.UNKNOWN;

    private LightRelayData relayData;

    public LightRelayView(Context context) {
        super(context);
        initializeViews(context);
    }

    public LightRelayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public LightRelayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    public void setDragMode(boolean dragMode) {
        this.dragMode = dragMode;
    }

//    public static String actionToString(int action) {
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                return "ACTION_DOWN";
//            case MotionEvent.ACTION_UP:
//                return "ACTION_UP";
//            case MotionEvent.ACTION_CANCEL:
//                return "ACTION_CANCEL";
//            case MotionEvent.ACTION_OUTSIDE:
//                return "ACTION_OUTSIDE";
//            case MotionEvent.ACTION_MOVE:
//                return "ACTION_MOVE";
//            case MotionEvent.ACTION_HOVER_MOVE:
//                return "ACTION_HOVER_MOVE";
//            case MotionEvent.ACTION_SCROLL:
//                return "ACTION_SCROLL";
//            case MotionEvent.ACTION_HOVER_ENTER:
//                return "ACTION_HOVER_ENTER";
//            case MotionEvent.ACTION_HOVER_EXIT:
//                return "ACTION_HOVER_EXIT";
//            case MotionEvent.ACTION_BUTTON_PRESS:
//                return "ACTION_BUTTON_PRESS";
//            case MotionEvent.ACTION_BUTTON_RELEASE:
//                return "ACTION_BUTTON_RELEASE";
//        }
//        int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//        switch (action & MotionEvent.ACTION_MASK) {
//            case MotionEvent.ACTION_POINTER_DOWN:
//                return "ACTION_POINTER_DOWN(" + index + ")";
//            case MotionEvent.ACTION_POINTER_UP:
//                return "ACTION_POINTER_UP(" + index + ")";
//            default:
//                return Integer.toString(action);
//        }
//    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.light_relay_layout, this);
        setTag(false);

        setOnTouchListener(new OnTouchListener() {
                               @Override
                               public boolean onTouch(View v, MotionEvent event) {
                                   if (!dragMode) {
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

    final Handler longPressHandle = new Handler();
    final Runnable longPressCall = new Runnable() {
        @Override
        public void run() {
            if (isPressed)
                onLongPress();
        }
    };

    private void onClick() {
        ((ChaActivity) getContext()).publish(String.format(Locale.US, "chac/light/state/%01X", relayData.getId()), buttonState == ButtonState.OFF ? "1" : "0", false);
        setState(ButtonState.WAIT);
    }

    private void onLongPress() {
        final CardView card = ((CardView) getChildAt(0));

        card.setCardBackgroundColor(Color.GRAY);
        PopupMenu popupMenu = new PopupMenu(getContext(), card);
        setTag(true);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                setTag(false);
                if (relayData != null) {
                    Intent intent = new Intent(getContext(), LogBooleanActivity.class);
                    intent.putExtra("id", relayData.getId());
                    intent.putExtra("scope", "LightRelay");
                    getContext().startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                setTag(false);
                card.setCardBackgroundColor(Color.WHITE);
            }
        });
        popupMenu.inflate(R.menu.light_relay_popup_menu);
        popupMenu.show();
    }

    private void setRelayName(CharSequence value) {
        ((TextView) this.findViewById(R.id.relay_name)).setText(value);
    }

    public void setIsOn(boolean value) {
        setState(value ? ButtonState.ON : ButtonState.OFF);
    }

    public void setIsPressed(boolean pressed) {
        this.isPressed = pressed;
        ((CardView) getChildAt(0)).setCardBackgroundColor(pressed ? Color.LTGRAY : Color.WHITE);
    }

    private void setState(ButtonState value) {
        buttonState = value;
        //getOnOffButton();

        Utils.disableOnCheckedListener = true;
        try {
            switch (value) {
                case UNKNOWN:
                    ((ImageView) findViewById(R.id.relay_light)).setImageResource(R.drawable.button_onoff_indicator_unknown);
                    setEnabled(false);
                    break;
                case ON:
                    ((ImageView) findViewById(R.id.relay_light)).setImageResource(R.drawable.button_onoff_indicator_on);
                    setEnabled(true);
                    break;
                case OFF:
                    ((ImageView) findViewById(R.id.relay_light)).setImageResource(R.drawable.button_onoff_indicator_off);
                    setEnabled(true);
                    break;
                case WAIT:
                    ((ImageView) findViewById(R.id.relay_light)).setImageResource(R.drawable.button_onoff_indicator_wait);
                    setEnabled(false);
                    setEnabled(true);//// TODO: 2/12/2017
                    break;
            }
        } finally {
            Utils.disableOnCheckedListener = false;
        }
    }

    public LightRelayData getRelayData() {
        return this.relayData;
    }

    public void setRelayData(LightRelayData value) {
        this.relayData = value;

        setRelayName(value.getName()); // + ", order=" + String.valueOf(value.getOrder()));
        //setComment(value.getComment());
        setIsOn(value.isOn());
    }
}