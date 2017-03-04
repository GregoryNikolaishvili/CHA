package ge.altasoft.gia.cha.other;

import android.content.Context;

import ge.altasoft.gia.cha.classes.ChaWidget;
import ge.altasoft.gia.cha.classes.DashboardItems;
import ge.altasoft.gia.cha.views.OutsideSensorView;
import ge.altasoft.gia.cha.views.PressureSensorView;
import ge.altasoft.gia.cha.views.RainSensorView;
import ge.altasoft.gia.cha.views.WindSensorView;

public final class OtherControllerData {

    final static public int _5IN1_SENSOR_ID_TH = 0;
    final static public int _5IN1_SENSOR_ID_WIND = 1;
    final static public int _5IN1_SENSOR_ID_PRESSURE = 2;
    final static public int _5IN1_SENSOR_ID_RAIN = 3;

    final static private int SENSOR_COUNT = 4;

    final public static OtherControllerData Instance = new OtherControllerData();

    private Sensor5in1Data sensor5in1Data;

    private OtherControllerData() {

        sensor5in1Data = new Sensor5in1Data(0); // 5 sensors
    }

    public int sensorCount() {
        return SENSOR_COUNT;
    }

    public ChaWidget createWidget(Context context, int position, boolean fromDashboard) {
        switch (position) {
            case _5IN1_SENSOR_ID_TH:
                return new OutsideSensorView(context, fromDashboard);
            case _5IN1_SENSOR_ID_WIND:
                return new WindSensorView(context, fromDashboard);
            case _5IN1_SENSOR_ID_PRESSURE:
                return new PressureSensorView(context, fromDashboard);
            case _5IN1_SENSOR_ID_RAIN:
                return new RainSensorView(context, fromDashboard);

            default:
                return null;
        }
    }

    public Sensor5in1Data get5in1SensorData() {
        return this.sensor5in1Data;
    }

    public static void saveWidgetOrders() {
        DashboardItems.saveWidgetOrders();
    }


    public static void restoreWidgetOrders(Context context) {
        DashboardItems.restoreWidgetOrders(context);
    }

    public boolean widgetOrderChanged() {
        return DashboardItems.widgetOrderChanged();
    }
}
