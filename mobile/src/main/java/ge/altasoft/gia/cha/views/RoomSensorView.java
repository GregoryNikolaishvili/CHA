package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import ge.altasoft.gia.cha.LogTHActivity;
import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.ChaCard;
import ge.altasoft.gia.cha.classes.DashboardItems;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;

public class RoomSensorView extends ChaCard {

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

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_sensor_layout, this);

        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final CardView card = ((CardView) ((ViewGroup) v).getChildAt(0));

                card.setCardBackgroundColor(Utils.getCardBackgroundColor(getContext(), true, false));
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_pin_to_dashboard:
                                if (DashboardItems.hasItem(1, sensorData.getId()))
                                    DashboardItems.remove(getContext(), 1, sensorData.getId());
                                else
                                    DashboardItems.add(getContext(), 1, sensorData.getId());
                                break;
                            case R.id.item_log:
                                if (sensorData != null) {
                                    Intent intent = new Intent(getContext(), LogTHActivity.class);
                                    intent.putExtra("id", sensorData.getId());
                                    intent.putExtra("scope", "RoomSensor");
                                    getContext().startActivity(intent);
                                }
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        card.setCardBackgroundColor(Utils.getCardBackgroundColor(getContext(), false, false));
                    }
                });
                popupMenu.inflate(R.menu.sensor_popup_menu);
                popupMenu.getMenu().findItem(R.id.item_pin_to_dashboard).setChecked(DashboardItems.hasItem(1, sensorData.getId()));
                popupMenu.show();

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


    public RoomSensorData getSensorData() {
        return this.sensorData;
    }

    public void setSensorData(RoomSensorData value) {
        this.sensorData = value;

        getSensorNameTextView().setText(value.getName() + ", order=" + String.valueOf(value.getOrder()));

        getTemperatureTextView();
        float v = value.getTemperature();
        if (Float.isNaN(v))
            tvTemperature.setText("- - - -");
        else
            tvTemperature.setText(String.format(Locale.US, "%.1f°", v));
        tvTemperature.setTextColor(value.getTemperatureColor());

        switch (value.getTemperatureTrend()) {
            case '=':
                getTemperatureTrendTextView().setText("");
                break;
            case '+':
                getTemperatureTrendTextView().setText("↑"); // ▲
                getTemperatureTrendTextView().setTextColor(0xFFFF3000);
                break;
            case '-':
                getTemperatureTrendTextView().setText("↓"); // ▼
                getTemperatureTrendTextView().setTextColor(0xFF0050FF);
                break;
        }

        v = value.getTargetTemperature();
        if (Float.isNaN(v))
            getTargetTemperatureTextView().setText("- - - -");
        else
            getTargetTemperatureTextView().setText(String.format(Locale.US, "%.1f°", v));

        v = value.getHumidity();
        if (Float.isNaN(v))
            getHumidityTextView().setText("- - - -");
        else
            getHumidityTextView().setText(String.format(Locale.US, "%.0f%%", v));

        getSignalLevelTextView().setText(String.valueOf(value.getSignalLevel()));

        getBatteryLevelTextView().setText(value.getBatteryLevel());

        getRelayStateTextView();

        tvRelayState.setVisibility(value.hasRelay() ? VISIBLE : INVISIBLE);
        if (value.isOn()) {
            tvRelayState.setTextColor(Color.YELLOW);
            tvRelayState.setText("On");
        } else {
            tvRelayState.setTextColor(Color.GRAY);
            tvRelayState.setText("Off");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (value.getLastSyncTime() < calendar.getTime().getTime())
            ((CardView) getChildAt(0)).setCardBackgroundColor(Utils.getCardBackgroundColor(getContext(), false, true));
        else
            ((CardView) getChildAt(0)).setCardBackgroundColor(Utils.getCardBackgroundColor(getContext(), false, false));
    }
}