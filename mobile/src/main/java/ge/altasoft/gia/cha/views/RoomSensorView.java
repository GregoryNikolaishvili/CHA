package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import ge.altasoft.gia.cha.LogTHActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;

public class RoomSensorView extends LinearLayout {

    private TextView tvSensorName;
    private TextView tvTemperature;
    private TextView tvTemperatureTrend;
    private TextView tvHumidity;

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

        getTemperatureTextView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (sensorData != null) {
                    Intent intent = new Intent(getContext(), LogTHActivity.class);
                    intent.putExtra("id", sensorData.getId());
                    intent.putExtra("scope", "RoomSensorT");
                    getContext().startActivity(intent);
                }
                return true;
            }
        });

        getHumidityTextView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (sensorData != null) {
                    Intent intent = new Intent(getContext(), LogTHActivity.class);
                    intent.putExtra("id", sensorData.getId());
                    intent.putExtra("scope", "RoomSensorH");
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

    private TextView getHumidityTextView() {
        if (tvHumidity == null)
            tvHumidity = (TextView) this.findViewById(R.id.humidity_value);
        return tvHumidity;
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
        tvTemperature.setText(String.format(Locale.US, "%.1f°", value.getTemperature()));

        tvTemperature.setTextColor(value.getTemperatureColor());

        switch (value.getTemperatureTrend()) {
            case RoomSensorData.NO_CHANGE:
                getTemperatureTrendTextView().setText("");
                break;
            case RoomSensorData.GOING_UP:
                getTemperatureTrendTextView().setText("↑"); // ▲
                getTemperatureTrendTextView().setTextColor(Color.RED);
                break;
            case RoomSensorData.GOING_DOWN:
                getTemperatureTrendTextView().setText("↓"); // ▼
                getTemperatureTrendTextView().setTextColor(Color.BLUE);
                break;
        }
        getHumidityTextView().setText(String.format(Locale.US, "%.0f%%", value.getHumidity()));
    }
}