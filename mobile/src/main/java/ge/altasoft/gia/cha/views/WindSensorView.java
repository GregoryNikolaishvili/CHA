package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import ge.altasoft.gia.cha.R;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.other.OtherControllerData;
import ge.altasoft.gia.cha.other.Sensor5in1Data;

public class WindSensorView extends ChaWidget {

    private TextView tvWindSpeed;
    private TextView tvMaxWindSpeed;
    private ImageView tvWindDirection;

    private int prevRotation = -Integer.MAX_VALUE;

    public WindSensorView(Context context, boolean fromDashboard) {
        super(context, fromDashboard);
        initializeViews(context);
    }

    public WindSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public WindSensorView(Context context, AttributeSet attrs, int defStyle) {
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
        return WidgetType.WindSensor;
    }

    @Override
    public int getWidgetId() {
        return OtherControllerData._5IN1_SENSOR_ID_WIND;
    }

    @Override
    protected int getPopupMenuResId() {
        return R.menu._5in1_sensor_popup_menu;
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.wind_sensor_layout, this);

        afterInflate();

        tvWindSpeed = (TextView) this.findViewById(R.id.wind_speed_value);
        tvMaxWindSpeed = (TextView) this.findViewById(R.id.wind_speed_max_value);
        tvWindDirection = (ImageView) this.findViewById(R.id.wind_speed_direction);
    }

    @Override
    public void refresh() {
        Sensor5in1Data data = OtherControllerData.Instance.get5in1SensorData();

        tvWindSpeed.setText(String.format(Locale.US, "%d", data.getWindSpeed()));

        if (tvWindDirection != null) {
            if (prevRotation != data.getWindDirection()) {
                RelativeLayout arrow_layout = (RelativeLayout) findViewById(R.id.arrow_layout);
                int arrow_sz = arrow_layout.getHeight() * 9 / 10;
                if (arrow_sz > 0) {
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(arrow_sz, arrow_sz);
                    lp.setMargins(
                            (arrow_layout.getWidth() - arrow_sz) / 2,
                            (arrow_layout.getHeight() - arrow_sz) / 3,
                            0,
                            0);
                    tvWindDirection.setLayoutParams(lp);
                    tvWindDirection.setRotation(data.getWindDirection());
                    prevRotation = data.getWindDirection();
                }
            }
        }

        tvMaxWindSpeed.setText(String.format(Locale.US, "%d km/h at %s", data.getMaxWindSpeed(), data.getMaxWindSpeedTime()));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (data.getRainWindPressureSyncTime() < calendar.getTime().getTime())
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, true));
        else
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));
    }
}