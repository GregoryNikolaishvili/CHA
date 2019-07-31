package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.other.WaterLevelData;

public class WaterLevelSensorView extends ChaWidget {

//    private enum ButtonState {UNKNOWN, ON, OFF, WAIT}

    private TextView tvSensorName;
    private TextView tvPercent;
    private TextView tvDistance;
    private TextView tvBallValve;
    private TextView tvBallValveSwitch;
    //private ImageView ivLight;

    private int defaultTextColor;

    private WaterLevelData waterLevelData;

//    private ButtonState buttonState = ButtonState.UNKNOWN;

    public WaterLevelSensorView(Context context, boolean fromDashboard) {
        super(context, fromDashboard);
        initializeViews(context);
    }

    public WaterLevelSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public WaterLevelSensorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    @Override
    protected boolean canClick() {
        return false;
    }

    @Override
    protected void onClick() {

    }

    @Override
    public WidgetType getWidgetType() {
        return WidgetType.WaterLevelSensor;
    }

    @Override
    public int getWidgetId() {
        return waterLevelData.getId();
    }

    @Override
    protected int getPopupMenuResId() {
        return R.menu._5in1_sensor_popup_menu;
    }

//    @Override
//    protected void onClick() {
//        ((ChaActivity) getContext()).publish(String.format(Locale.US, "chac/lc/state/%01X", relayData.getId()), buttonState == ButtonState.OFF ? "1" : "0", false);
//        setState(ButtonState.WAIT);
//    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.waterlevel_sensor_layout, this);

        afterInflate();

        tvSensorName = (TextView) this.findViewById(R.id.sensor_name);
        tvPercent = (TextView) this.findViewById(R.id.water_percent_value);
        defaultTextColor = tvPercent.getCurrentTextColor();

        tvDistance = (TextView) this.findViewById(R.id.distance_cm);
        tvBallValve = (TextView) this.findViewById(R.id.ball_valve_state);
        tvBallValveSwitch = (TextView) this.findViewById(R.id.ball_valve_switch_state);

        //ivLight = ((ImageView) findViewById(R.id.relay_light));
    }

//    private void setState(ButtonState value) {
//        buttonState = value;
//
//        switch (value) {
//            case UNKNOWN:
//                ivLight.setImageResource(R.drawable.button_onoff_indicator_unknown);
//                break;
//            case ON:
//                ivLight.setImageResource(R.drawable.button_onoff_indicator_on);
//                break;
//            case OFF:
//                ivLight.setImageResource(R.drawable.button_onoff_indicator_off);
//                break;
//            case WAIT:
//                ivLight.setImageResource(R.drawable.button_onoff_indicator_wait);
//                break;
//        }
//    }

    public void setWaterLevelData(WaterLevelData value) {
        this.waterLevelData = value;
        refresh();
    }

    @Override
    public void refresh() {
        tvSensorName.setText(getResources().getString(R.string.water_level) + " " + (this.waterLevelData.getId() + 1));

        int x = this.waterLevelData.getWaterPercent();
        if (x == 255)
            tvPercent.setText("- - - - %");
        else
            tvPercent.setText(String.format(Locale.US, "%d %%", x));

        if (this.waterLevelData.getFloatSwitchIsOn())
            tvPercent.setTextColor(Color.CYAN);
        else
            tvPercent.setTextColor(defaultTextColor);

        x = this.waterLevelData.getWaterDistance();
        if (x == Utils.I_UNDEFINED)
            tvDistance.setText("- - cm");
        else
            tvDistance.setText(String.format(Locale.US, "%d cm", x));

        int state = this.waterLevelData.getBallValveState();

        tvBallValve.setText(Utils.GetBallValveStateText(state));
        tvBallValve.setTextColor(Utils.GetBallValveStateColor(state));

        char sstate = this.waterLevelData.getBallValveSwitchState();
        String text = "";
        switch (sstate) {
            case 'O':
                text = "Open";
                break;
            case 'C':
                text = "Closed";
                break;
            case 'B':
                text = "Open/Closed";
                break;
        }
        tvBallValveSwitch.setText(text);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (this.waterLevelData.getLastSyncTime() < calendar.getTimeInMillis())
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, true));
        else
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));
    }

    @Override
    protected long getLastSyncTime() {
        return this.waterLevelData.getLastSyncTime();
    }
}