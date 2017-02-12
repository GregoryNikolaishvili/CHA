package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import ge.altasoft.gia.cha.LogTHActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;

public class RoomSensorView extends LinearLayout {

    private TextView tvSensorName;
    private TextView tvTemperature;
    private TextView tvTemperatureTrend;
    private TextView tvTargetTemperature;
    private TextView tvHumidity;
    private TextView tvSignalLevel;
    private TextView tvBatteryLevel;
    private TextView tvRelayState;

    private RoomSensorData sensorData;

    public RoomSensorView(Context context) {
        super(context);
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

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_sensor_layout, this);

        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (sensorData != null) {
                    Intent intent = new Intent(getContext(), LogTHActivity.class);
                    intent.putExtra("id", sensorData.getId());
                    intent.putExtra("scope", "RoomSensor");
                    getContext().startActivity(intent);
                }
                return true;
            }
        });
    }

    private TextView getSensorNameTextView() {
        if (tvSensorName == null)
            tvSensorName = (TextView) this.findViewById(R.id.sensor_name);
        return tvSensorName;
    }

    private TextView getTemperatureTextView() {
        if (tvTemperature == null)
            tvTemperature = (TextView) this.findViewById(R.id.temperature_value);
        return tvTemperature;
    }

    private TextView getTemperatureTrendTextView() {
        if (tvTemperatureTrend == null)
            tvTemperatureTrend = (TextView) this.findViewById(R.id.temperature_trend);
        return tvTemperatureTrend;
    }

    private TextView getTargetTemperatureTextView() {
        if (tvTargetTemperature == null)
            tvTargetTemperature = (TextView) this.findViewById(R.id.target_temperature_value);
        return tvTargetTemperature;
    }

    private TextView getHumidityTextView() {
        if (tvHumidity == null)
            tvHumidity = (TextView) this.findViewById(R.id.humidity_value);
        return tvHumidity;
    }

    private TextView getSignalLevelTextView() {
        if (tvSignalLevel == null)
            tvSignalLevel = (TextView) this.findViewById(R.id.signal_level);
        return tvSignalLevel;
    }

    private TextView getBatteryLevelTextView() {
        if (tvBatteryLevel == null)
            tvBatteryLevel = (TextView) this.findViewById(R.id.battery_value);
        return tvBatteryLevel;
    }

    private TextView getRelayStateTextView() {
        if (tvRelayState == null)
            tvRelayState = (TextView) this.findViewById(R.id.relay_state);
        return tvRelayState;
    }


//    public void setSensorName(CharSequence value) {
//        getSensorNameTextView().setText(value);
//    }
//
//    public void setTemperature(double value) {
//    }
//
//    public void setHumidity(double value) {
//        getHumidityTextView().setText(String.format(Locale.US, "%.0f%%", value));
//    }

    public RoomSensorData getSensorData() {
        return this.sensorData;
    }

    public void setSensorData(RoomSensorData value) {
        this.sensorData = value;

        getSensorNameTextView().setText(value.getName() + ", order=" + String.valueOf(value.getOrder()));

        getTemperatureTextView();
        float v = value.getTemperature();
        if (Float.isNaN(v))
            tvTemperature.setText("--");
        else
            tvTemperature.setText(String.format(Locale.US, "%.1f°", v));
        tvTemperature.setTextColor(value.getTemperatureColor());

        switch (value.getTemperatureTrend()) {
            case '=':
                getTemperatureTrendTextView().setText("");
                break;
            case '+':
                getTemperatureTrendTextView().setText("↑"); // ▲
                getTemperatureTrendTextView().setTextColor(Color.RED);
                break;
            case '-':
                getTemperatureTrendTextView().setText("↓"); // ▼
                getTemperatureTrendTextView().setTextColor(Color.BLUE);
                break;
        }

        v = value.getTargetTemperature();
        if (Float.isNaN(v))
            getTargetTemperatureTextView().setText("--");
        else
            getTargetTemperatureTextView().setText(String.format(Locale.US, "%.1f°", v));

        v = value.getHumidity();
        if (Float.isNaN(v))
            getHumidityTextView().setText("--");
        else
            getHumidityTextView().setText(String.format(Locale.US, "%.0f%%", v));

        getSignalLevelTextView().setText(String.valueOf(value.getSignalLevel()));

        getBatteryLevelTextView().setText(value.getBatteryLevel());

        getRelayStateTextView();

        tvRelayState.setVisibility(value.hasRelay() ? VISIBLE : INVISIBLE);
        if (value.isOn()) {
            tvRelayState.setTextColor(Color.RED);
            tvRelayState.setText("On");
        } else {
            tvRelayState.setTextColor(Color.GRAY);
            tvRelayState.setText("Off");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (value.getLastReadingTime() < calendar.getTime().getTime())
            this.getChildAt(0).setBackgroundResource(R.drawable.rounded_border_red);
        else
            this.getChildAt(0).setBackgroundResource(R.drawable.rounded_border);
    }
}