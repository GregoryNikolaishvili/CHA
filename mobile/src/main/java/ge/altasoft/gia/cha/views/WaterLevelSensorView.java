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

    private TextView tvPercent;
    private TextView tvDistance;
    private TextView tvSolenoid;

    private int defaultTextColor;

    private WaterLevelData waterLevelData;

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

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.waterlevel_sensor_layout, this);

        afterInflate();

        tvPercent = (TextView) this.findViewById(R.id.water_percent_value);
        defaultTextColor = tvPercent.getCurrentTextColor();

        tvDistance = (TextView) this.findViewById(R.id.distance_cm);
        tvSolenoid = (TextView) this.findViewById(R.id.solenoid_state);
    }

    public void setWaterLevelData(WaterLevelData value) {
        this.waterLevelData = value;
        refresh();
    }

    @Override
    public void refresh() {
        tvPercent.setText(String.format(Locale.US, "%d %%", this.waterLevelData.getWaterPercent()));
        if (this.waterLevelData.getFloatSwitchIsOn())
            tvPercent.setTextColor(Color.CYAN);
        else
            tvPercent.setTextColor(defaultTextColor);

        tvDistance.setText(String.format(Locale.US, "%d cm", this.waterLevelData.getWaterDistance()));

        if (this.waterLevelData.getSolenoidIsOn()) {
            tvSolenoid.setText("On");
            tvSolenoid.setTextColor(Color.GREEN);
        } else {
            tvSolenoid.setText("Off");
            tvSolenoid.setTextColor(Color.RED);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (this.waterLevelData.getLastSyncTime() < calendar.getTime().getTime())
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, true));
        else
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));
    }
}