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
    private TextView tvTargetTemperature;
    private LinearLayout llTargetTemperature;

    private BoilerSensorData sensorData;

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

    private TextView getTargetTemperatureTextView() {
        if (tvTargetTemperature == null)
            tvTargetTemperature = (TextView) this.findViewById(R.id.target_temperature_value);
        return tvTargetTemperature;
    }

    private LinearLayout getTargetTemperatureLayout() {
        if (llTargetTemperature == null)
            llTargetTemperature = (LinearLayout) this.findViewById(R.id.target_temperature);
        return llTargetTemperature;
    }

    public void setSensorData(BoilerSensorData value) {
        this.sensorData = value;

        getTemperatureTextView();
        getTemperatureTrendTextView();
        getTargetTemperatureLayout();

        float v = value.getTemperature();
        if (Float.isNaN(v))
            tvTemperature.setText("--");
        else
            tvTemperature.setText(String.format(Locale.US, "%.1f°", value.getTemperature()));
        tvTemperature.setTextColor(value.getTemperatureColor());

        switch (value.getTemperatureTrend()) {
            case '+':
                tvTemperatureTrend.setText("↑");
                tvTemperatureTrend.setTextColor(Color.RED);
                break;
            case '-':
                tvTemperatureTrend.setText("↓");
                tvTemperatureTrend.setTextColor(Color.BLUE);
                break;
        }

        v = value.getTargetTemperature();
        if (Float.isNaN(v))
            llTargetTemperature.setVisibility(View.GONE);
        else {
            getTargetTemperatureTextView();
            tvTargetTemperature.setText(String.format(Locale.US, "%.1f°", value.getTargetTemperature()));
            llTargetTemperature.setVisibility(View.VISIBLE);
        }
    }
}