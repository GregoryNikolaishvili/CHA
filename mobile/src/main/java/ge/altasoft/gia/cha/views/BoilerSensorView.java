package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.LogTHActivity;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.ChaCard;
import ge.altasoft.gia.cha.classes.DashboardItems;
import ge.altasoft.gia.cha.thermostat.BoilerSensorData;

public class BoilerSensorView extends ChaCard {

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

    private void initializeViews(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(getIsFromDashboard() ? R.layout.boiler_sensor_layout2 : R.layout.boiler_sensor_layout, this);

        getChildAt(0).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final CardView card = (CardView) v;

                card.setCardBackgroundColor(Utils.getCardBackgroundColor(context, true, false));
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_pin_to_dashboard:
                                if (DashboardItems.hasItem(2, sensorData.getId()))
                                    DashboardItems.remove(getContext(), 2, sensorData.getId());
                                else
                                    DashboardItems.add(getContext(), 2, sensorData.getId());
                                break;
                            case R.id.item_log:
                                if (sensorData != null) {
                                    Intent intent = new Intent(getContext(), LogTHActivity.class);
                                    intent.putExtra("id", sensorData.getId());
                                    intent.putExtra("scope", "BoilerSensor");
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
                        card.setCardBackgroundColor(Utils.getCardBackgroundColor(context, false, false));
                    }
                });
                popupMenu.inflate(R.menu.sensor_popup_menu);
                popupMenu.getMenu().findItem(R.id.item_pin_to_dashboard).setChecked(DashboardItems.hasItem(2, sensorData.getId()));
                popupMenu.show();

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

    public BoilerSensorData getSensorData() {
        return this.sensorData;
    }

    public void setSensorData(BoilerSensorData value) {
        this.sensorData = value;

        getTemperatureTextView();
        getTemperatureTrendTextView();
        getTargetTemperatureLayout();

        float v = value.getTemperature();
        if (Float.isNaN(v))
            tvTemperature.setText("- - - -");
        else
            tvTemperature.setText(String.format(Locale.US, "%.1f°", value.getTemperature()));
        tvTemperature.setTextColor(value.getTemperatureColor());

        switch (value.getTemperatureTrend()) {
            case '+':
                tvTemperatureTrend.setText("↑");
                tvTemperatureTrend.setTextColor(0xFFFF3000);
                break;
            case '-':
                tvTemperatureTrend.setText("↓");
                tvTemperatureTrend.setTextColor(0xFF0050FF);
                break;
            case '=':
                tvTemperatureTrend.setText("");
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

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (value.getLastSyncTime() < calendar.getTime().getTime())
            ((CardView) getChildAt(0)).setCardBackgroundColor(Utils.getCardBackgroundColor(getContext(), false, true));
        else
            ((CardView) getChildAt(0)).setCardBackgroundColor(Utils.getCardBackgroundColor(getContext(), false, false));

        if (getIsFromDashboard())
            ((TextView) findViewById(R.id.boiler_sensor_caption)).setText("T".concat(String.valueOf(value.getId() + 1)));
    }
}