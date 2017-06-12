package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.thermostat.BoilerSensorData;

public class BoilerSensorView extends ChaWidget {

    private TextView tvTemperature;
    private TextView tvTemperatureTrend;
    private TextView tvTargetTemperature;
    private LinearLayout llTargetTemperature;

    private BoilerSensorData sensorData;

    public BoilerSensorView(Context context, boolean fromDashboard) {
        super(context, fromDashboard);
        initializeViews(context);
    }

    public BoilerSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public BoilerSensorView(Context context, AttributeSet attrs, int defStyle) {
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
        return WidgetType.BoilerSensor;
    }

    @Override
    public int getWidgetId() {
        return sensorData.getId();
    }

    @Override
    protected int getPopupMenuResId() {
        return R.menu.th_sensor_popup_menu;
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(getIsFromDashboard() ? R.layout.boiler_sensor_layout2 : R.layout.boiler_sensor_layout, this);

        afterInflate();

        tvTemperature = (TextView) this.findViewById(R.id.temperature_value);
        tvTemperatureTrend = (TextView) this.findViewById(R.id.temperature_trend);
        tvTargetTemperature = (TextView) this.findViewById(R.id.target_temperature_value);
        llTargetTemperature = (LinearLayout) this.findViewById(R.id.target_temperature);
    }

//    public BoilerSensorData getSensorData() {
//        return this.sensorData;
//    }

    public void setSensorData(BoilerSensorData value) {
        this.sensorData = value;
        refresh();
    }

    @Override
    public void refresh() {
        float v = this.sensorData.getTemperature();
        if (Float.isNaN(v))
            tvTemperature.setText("- - - -");
        else
            tvTemperature.setText(String.format(Locale.US, "%.0f°", this.sensorData.getTemperature()));
        tvTemperature.setTextColor(this.sensorData.getTemperatureColor());

        switch (this.sensorData.getTemperatureTrend()) {
            case '+':
                tvTemperatureTrend.setText("↑");
                tvTemperatureTrend.setTextColor(Utils.COLOR_TEMP_HIGH);
                break;
            case '-':
                tvTemperatureTrend.setText("↓");
                tvTemperatureTrend.setTextColor(Utils.COLOR_TEMP_LOW);
                break;
            case '=':
                tvTemperatureTrend.setText("");
                break;
        }

        v = this.sensorData.getTargetTemperature();
        if (Float.isNaN(v))
            llTargetTemperature.setVisibility(View.GONE);
        else {
            tvTargetTemperature.setText(String.format(Locale.US, "%.0f°", this.sensorData.getTargetTemperature()));
            llTargetTemperature.setVisibility(View.VISIBLE);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (this.sensorData.getLastSyncTime() < calendar.getTimeInMillis())
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, true));
        else
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));

        if (getIsFromDashboard())
            ((TextView) findViewById(R.id.boiler_sensor_caption)).setText("T".concat(String.valueOf(this.sensorData.getId() + 1)));
    }

    @Override
    protected long getLastSyncTime()
    {
        return this.sensorData.getLastSyncTime();
    }

}