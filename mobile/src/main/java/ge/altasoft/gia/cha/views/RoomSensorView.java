package ge.altasoft.gia.cha.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

//import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER;
//import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER_AWAY;
//import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER_POOL;
//import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_SUMMER_POOL_AWAY;
import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_WINTER;
import static ge.altasoft.gia.cha.thermostat.BoilerSettings.BOILER_MODE_WINTER_AWAY;

public class RoomSensorView extends ChaWidget {

    private TextView tvSensorName;
    private TextView tvTemperature;
    private TextView tvTemperatureTrend;
    private TextView tvTargetTemperature;
    private TextView tvHumidity;
    private TextView tvSignalLevel;
    private TextView tvBatteryLevel;
    private TextView tvRelayState;

    private RoomSensorData sensorData;

    public RoomSensorView(Context context, boolean fromDashboard) {
        super(context, fromDashboard);
        initializeViews(context);
    }

    public RoomSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public RoomSensorView(Context context, AttributeSet attrs, int defStyle) {
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
        return WidgetType.RoomSensor;
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
        inflater.inflate(R.layout.room_sensor_layout, this);

        afterInflate();

        tvSensorName = (TextView) this.findViewById(R.id.sensor_name);
        tvTemperature = (TextView) this.findViewById(R.id.temperature_value);
        tvTemperatureTrend = (TextView) this.findViewById(R.id.temperature_trend);
        tvTargetTemperature = (TextView) this.findViewById(R.id.target_temperature_value);
        tvHumidity = (TextView) this.findViewById(R.id.humidity_value);
        tvSignalLevel = (TextView) this.findViewById(R.id.signal_level);
        tvBatteryLevel = (TextView) this.findViewById(R.id.battery_value);
        tvRelayState = (TextView) this.findViewById(R.id.relay_state);
    }

    public void setSensorData(RoomSensorData value) {
        this.sensorData = value;
        refresh();
    }

    @Override
    public void refresh() {
        tvSensorName.setText(this.sensorData.getName()); // + ", order=" + String.valueOf(this.sensorData.getOrder()));

        float v = this.sensorData.getTemperature();
        if (Float.isNaN(v))
            tvTemperature.setText("- - - -");
        else
            tvTemperature.setText(String.format(Locale.US, "%.1f°", v));
        tvTemperature.setTextColor(this.sensorData.getTemperatureColor());

        switch (this.sensorData.getTemperatureTrend()) {
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

        v = this.sensorData.getTargetTemperature();
        if (Float.isNaN(v))
            tvTargetTemperature.setText("- - - -");
        else
            tvTargetTemperature.setText(String.format(Locale.US, "%.1f°", v));

        v = this.sensorData.getHumidity();
        if (Float.isNaN(v))
            tvHumidity.setText("- - - -");
        else
            tvHumidity.setText(String.format(Locale.US, "%.0f%%", v));

        tvSignalLevel.setText(String.valueOf(this.sensorData.getSignalLevel()));

        tvBatteryLevel.setText(this.sensorData.getBatteryLevel());

        tvRelayState.setVisibility(this.sensorData.hasRelay() ? VISIBLE : INVISIBLE);

        if (this.sensorData.Value() > 0) {
            char mode = ThermostatControllerData.Instance.getBoilerMode();
            if (mode == BOILER_MODE_WINTER || mode == BOILER_MODE_WINTER_AWAY)
                tvRelayState.setTextColor(Color.YELLOW);
            tvRelayState.setText(this.sensorData.Value() + "%");
        } else {
            tvRelayState.setTextColor(Color.GRAY);
            tvRelayState.setText("Off");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (this.sensorData.getLastSyncTime() < calendar.getTimeInMillis())
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, true));
        else
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));
    }

    @Override
    protected void menuItemClick(MenuItem item) {
        super.menuItemClick(item);

        switch (item.getItemId()) {
            case R.id.item_set_temperature:
                final Dialog dialog = new Dialog(this.getContext());
                dialog.setContentView(R.layout.dialog_emperature);
                dialog.setTitle("Set target temperature");

                // set the custom dialog components - text, image and button
                final TextView tvText = (TextView) dialog.findViewById(R.id.dlg_temperature_value);
                tvText.setText(String.format(Locale.US, "%.1f", (float) this.sensorData.getTargetTemperature()));

                Button okBtn = (Button) dialog.findViewById(R.id.dialogButtonOK);
                ImageButton minusBtn = (ImageButton) dialog.findViewById(R.id.dialogButtonMinus);
                ImageButton plusBtn = (ImageButton) dialog.findViewById(R.id.dialogButtonPlus);

                minusBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tt = tvText.getText().toString();
                        float t = parseTemperature(tt);
                        t -= 0.1f;
                        tvText.setText(String.format(Locale.US, "%.1f", t));
                    }
                });

                plusBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tt = tvText.getText().toString();
                        float t = parseTemperature(tt);
                        t += 0.1f;
                        tvText.setText(String.format(Locale.US, "%.1f", t));
                    }
                });

                okBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
        }
    }

    private static Float parseTemperature(String text) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return 22.0f;
        }
    }

    @Override
    protected long getLastSyncTime() {
        return this.sensorData.getLastSyncTime();
    }
}