package ge.altasoft.gia.cha.views;

import android.content.Context;
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

public class RainSensorView extends ChaWidget {

    private TextView tvRain;
    private TextView tvDailyRain;

    public RainSensorView(Context context, boolean fromDashboard) {
        super(context, fromDashboard);
        initializeViews(context);
    }

    public RainSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public RainSensorView(Context context, AttributeSet attrs, int defStyle) {
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
        return WidgetType.RainSensor;
    }

    @Override
    public int getWidgetId() {
        return OtherControllerData._5IN1_SENSOR_ID_RAIN;
    }

    @Override
    protected int getPopupMenuResId() {
        return R.menu._5in1_sensor_popup_menu;
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.rain_sensor_layout, this);

        afterInflate();

        tvRain = (TextView) this.findViewById(R.id.rain_value);
        tvDailyRain = (TextView) this.findViewById(R.id.daily_rain_value);
    }

    @Override
    public void refresh() {
        Sensor5in1Data data = OtherControllerData.Instance.get5in1SensorData();

        tvRain.setText(String.format(Locale.US, "%d", data.getRain()));
        tvDailyRain.setText(String.format(Locale.US, "%d mm", data.getDailyRain()));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (data.getRainWindPressureSyncTime() < calendar.getTime().getTime())
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, true));
        else
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));
    }
}