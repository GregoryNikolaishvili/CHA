package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

    private TextView tvRelayName;
    //private TextView tvComment;
    //private ImageView tbButton;
    private boolean dragMode = false;

    private ButtonState buttonState = ButtonState.UNKNOWN;
    private ButtonState prevButtonState = ButtonState.UNKNOWN;

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

    public static String actionToString(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return "ACTION_DOWN";
            case MotionEvent.ACTION_UP:
                return "ACTION_UP";
            case MotionEvent.ACTION_CANCEL:
                return "ACTION_CANCEL";
            case MotionEvent.ACTION_OUTSIDE:
                return "ACTION_OUTSIDE";
            case MotionEvent.ACTION_MOVE:
                return "ACTION_MOVE";
            case MotionEvent.ACTION_HOVER_MOVE:
                return "ACTION_HOVER_MOVE";
            case MotionEvent.ACTION_SCROLL:
                return "ACTION_SCROLL";
            case MotionEvent.ACTION_HOVER_ENTER:
                return "ACTION_HOVER_ENTER";
            case MotionEvent.ACTION_HOVER_EXIT:
                return "ACTION_HOVER_EXIT";
            case MotionEvent.ACTION_BUTTON_PRESS:
                return "ACTION_BUTTON_PRESS";
            case MotionEvent.ACTION_BUTTON_RELEASE:
                return "ACTION_BUTTON_RELEASE";
        }
        int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                return "ACTION_POINTER_DOWN(" + index + ")";
            case MotionEvent.ACTION_POINTER_UP:
                return "ACTION_POINTER_UP(" + index + ")";
            default:
                return Integer.toString(action);
        }
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.light_relay_layout, this);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!dragMode) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            prevButtonState = buttonState;
                            setIsPressed(true);
                            break;
                        case MotionEvent.ACTION_UP:
                            setIsPressed(false);
                            ((ChaActivity) getContext()).publish(String.format(Locale.US, "chac/light/state/%01X", relayData.getId()), buttonState == ButtonState.OFF ? "1" : "0", false);
                            setState(ButtonState.WAIT);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            setIsPressed(false);
                            setState(prevButtonState);
                            break;
                    }
                    //Log.d("Touch", actionToString(event.getAction()));
                }
                return false;
            }
        });

//        getOnOffButton().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
//                if (!Utils.disableOnCheckedListener) {
//                    ((ToggleButton) button).setTextOn("");
//                    ((ToggleButton) button).setTextOff("");
//                    button.setEnabled(false);
//
//                    ((ChaActivity) getContext()).publish(String.format(Locale.US, "chac/light/state/%01X", relayData.getId()), isChecked ? "1" : "0", false);
//                    //LightUtils.sendCommandToController(getContext(), String.format(Locale.US, "#%01X%s", relayData.getId(), isChecked ? "1" : "0"));
//                }
//            }
//        });


        this.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (relayData != null) {
                    Intent intent = new Intent(getContext(), LogBooleanActivity.class);
                    intent.putExtra("id", relayData.getId());
                    intent.putExtra("scope", "LightRelay");
                    getContext().startActivity(intent);
                }
                return true;
            }
        });
    }


    private TextView getRelayNameTextView() {
        if (tvRelayName == null)
            tvRelayName = (TextView) this.findViewById(R.id.relay_name);
        return tvRelayName;
    }

//    private TextView getCommentTextView() {
//        if (tvComment == null)
//            tvComment = (TextView) this.findViewById(R.id.comment);
//        return tvComment;
//    }

//    private ImageView getOnOffButton() {
//        if (tbButton == null)
//            tbButton = (ImageView) this.findViewById(R.id.on_off_button);
//        return tbButton;
//    }

    private void setRelayName(CharSequence value) {
        getRelayNameTextView().setText(value);
    }

//    private void setComment(CharSequence value) {
//        getCommentTextView().setText(value);
//    }

    public void setIsOn(boolean value) {
        setState(value ? ButtonState.ON : ButtonState.OFF);
    }

    private void setIsPressed(Boolean value) {

        ((CardView) getChildAt(0)).setCardBackgroundColor(value ? Color.LTGRAY : Color.WHITE);
    }

    private void setState(ButtonState value) {
        buttonState = value;
        //getOnOffButton();

        Utils.disableOnCheckedListener = true;
        try {
            switch (value) {
                case UNKNOWN:
                    //tbButton.setImageResource(0);
                    ((CardView)findViewById(R.id.relay_light)).setCardBackgroundColor(0);
                    setEnabled(false);
                    break;
                case ON:
                    ((CardView)findViewById(R.id.relay_light)).setCardBackgroundColor(Color.GREEN);
                    //tbButton.setImageResource(R.drawable.rounded_rectangle_green);
                    setEnabled(true);
                    break;
                case OFF:
                    ((CardView)findViewById(R.id.relay_light)).setCardBackgroundColor(Color.GRAY);
                    //tbButton.setImageResource(R.drawable.rounded_rectangle_gray);
                    setEnabled(true);
                    break;
                case WAIT:
                    ((CardView)findViewById(R.id.relay_light)).setCardBackgroundColor(Color.YELLOW);
                    //tbButton.setImageResource(R.drawable.rounded_rectangle_yellow);
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