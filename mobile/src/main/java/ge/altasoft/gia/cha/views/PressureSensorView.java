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

public class PressureSensorView extends ChaWidget {

    private TextView tvPressure;

    public PressureSensorView(Context context, boolean fromDashboard) {
        super(context, fromDashboard);
        initializeViews(context);
    }

    public PressureSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public PressureSensorView(Context context, AttributeSet attrs, int defStyle) {
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
        return WidgetType.PressureSensor;
    }

    @Override
    public int getWidgetId() {
        return OtherControllerData._5IN1_SENSOR_ID_PRESSURE;
    }

    @Override
    protected int getPopupMenuResId() {
        return R.menu._5in1_sensor_popup_menu;
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.pressure_sensor_layout, this);

        afterInflate();

        tvPressure = (TextView) this.findViewById(R.id.pressure_value);
    }

    @Override
    public void refresh() {
        Sensor5in1Data data = OtherControllerData.Instance.get5in1SensorData();

        tvPressure.setText(String.format(Locale.US, "%d", data.getPressure()));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -2);
        if (data.getRainWindPressureSyncTime() < calendar.getTimeInMillis())
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, true));
        else
            cardView.setCardBackgroundColor(Utils.getCardBackgroundColor(false, false));
    }

    @Override
    protected long getLastSyncTime()
    {
        return OtherControllerData.Instance.get5in1SensorData().getRainWindPressureSyncTime();
    }

}