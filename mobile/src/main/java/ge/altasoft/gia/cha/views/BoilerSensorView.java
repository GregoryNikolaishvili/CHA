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

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.LogTHActivity;
import ge.altasoft.gia.cha.thermostat.BoilerSensorData;

public class BoilerSensorView extends LinearLayout {

    private TextView tvTemperature;
    private TextView tvTemperatureTrend;

    BoilerSensorData sensorData;

    public BoilerSensorView(Context context) {
        super(context);
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

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.boiler_sensor_layout, this);

        this.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (sensorData != null) {
                    Intent intent = new Intent(getContext(), LogTHActivity.class);
                    intent.putExtra("id", sensorData.getId());
                    intent.putExtra("scope", "BoilerSensor");
                    getContext().startActivity(intent);
                }
                return true;
            }
        });
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

    public void setSensorData(BoilerSensorData value) {
        this.sensorData = value;

        getTemperatureTextView();
        getTemperatureTrendTextView();

        tvTemperature.setText(String.format(Locale.US, "%.1f°", value.getTemperature()));
        tvTemperature.setTextColor(value.getTemperatureColor());

        switch (value.getTemperatureTrend()) {
            case BoilerSensorData.NO_CHANGE:
                tvTemperatureTrend.setText("");
                break;
            case BoilerSensorData.GOING_UP:
                tvTemperatureTrend.setText("↑");
                tvTemperatureTrend.setTextColor(Color.RED);
                break;
            case BoilerSensorData.GOING_DOWN:
                tvTemperatureTrend.setText("↓");
                tvTemperatureTrend.setTextColor(Color.BLUE);
                break;
        }
    }
}