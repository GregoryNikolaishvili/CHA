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
import ge.altasoft.gia.cha.other.OtherControllerData;
import ge.altasoft.gia.cha.other.Sensor5in1Data;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;

public class OutsideSensorView extends ChaWidget {

    private TextView tvTemperature;
    private TextView tvTemperatureTrend;
    private TextView tvHumidity;
    private TextView tvSignalLevel;
    private TextView tvBatteryLevel;

    public OutsideSensorView(Context context, boolean fromDashboard) {
        super(context, fromDashboard);
        initializeViews(context);
    }

    public OutsideSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public OutsideSensorView(Context context, AttributeSet attrs, int defStyle) {
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
        return WidgetType.OutsideSensor;
    }

    @Override
    public int getWidgetId() {
        return OtherControllerData._5IN1_SENSOR_ID_TH;
    }

    @Override
    protected int getPopupMenuResId() {
        return R.menu.sensor_popup_menu;
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.outside_sensor_layout, this);

        afterInflate();

        tvTemperature = (TextView) this.findViewById(R.id.temperature_value);
        tvTemperatureTrend = (TextView) this.findViewById(R.id.temperature_trend);
        tvHumidity = (TextView) this.findViewById(R.id.humidity_value);
        tvSignalLevel = (TextView) this.findViewById(R.id.signal_level);
        tvBatteryLevel = (TextView) this.findViewById(R.id.battery_value);
    }

    @Override
    public void refresh() {
        Sensor5in1Data data = OtherControllerData.Instance.get5in1SensorData();

        float v = data.getTemperature();
        if (Float.isNaN(v))
            tvTemperature.setText("- - - -");
        else
            tvTemperature.setText(String.format(Locale.US, "%.1f°", v));

        switch (data.getTemperatureTrend()) {
            case '=':
                tvTemperatureTrend.setText("");
                break;
            case '+':
                tvTemperatureTrend.setText("↑"); // ▲
                tvTemperatureTrend.setTextColor(Utils.COLOR_TEMP_HIGH);
                break;
            case '-':
                tvTemperatureTrend.setText("↓"); // ▼
                tvTemperatureTrend.setTextColor(Utils.COLOR_TEMP_LOW);
                break;
        }

        v = data.getHumidity();
        if (Float.isNaN(v))
            tvHumidity.setText("- - - -");
        else
            tvHumidity.setText(String.format(Locale.US, "%.0f%%", v));

        tvSignalLevel.setText(String.valueOf(data.getSignalLevel()));

        tvBatteryLevel.setText(data.getBatteryLevel());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (data.getLastSyncTime() < calendar.getTime().getTime())
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(getContext(), false, true));
        else
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(getContext(), false, false));
    }
}