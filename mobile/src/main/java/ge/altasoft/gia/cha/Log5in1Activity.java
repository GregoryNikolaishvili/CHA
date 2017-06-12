package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.classes.LogTwoValueItem;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.other.OtherControllerData;
import ge.altasoft.gia.cha.other.Sensor5in1Data;
import ge.altasoft.gia.cha.other.WaterLevelData;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class Log5in1Activity extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private WidgetType scope;
    private int widgetId;

    private _5in1LogAdapter adapter = null;
    private ArrayList<LogTwoValueItem> logBuffer;

    private GraphicalView mChartView;
    private final XYMultipleSeriesDataset xyDataSet = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_th);

        Intent intent = getIntent();
        scope = (WidgetType) intent.getSerializableExtra("widget");
        widgetId = intent.getIntExtra("id", -1);

        logBuffer = new ArrayList<>();
        adapter = new _5in1LogAdapter(this, logBuffer, (scope == WidgetType.WindSensor) || (scope == WidgetType.WaterLevelSensor));

        ListView listView = (ListView) findViewById(R.id.lvLog);
        listView.setAdapter(adapter);

        LinearLayout chartLayout = (LinearLayout) findViewById(R.id.chartLogBig);
        XYSeries series = new XYSeries(scope.name());

        mRenderer = ThermostatUtils.getSensorChartRenderer(this, false, 1, new int[]{Color.RED});
        mRenderer.setZoomEnabled(true, true);
        mRenderer.setPanEnabled(true, true);
        mRenderer.setZoomButtonsVisible(true);
        mRenderer.setShowLegend(true);

        //mRenderer.setPanLimits(new double[] { -10, 20, -10, 40 });
        //mRenderer.setZoomLimits(new double[] { -10, 20, -10, 40 });

        xyDataSet.addSeries(series);

        mChartView = ChartFactory.getCubeLineChartView(this, xyDataSet, mRenderer, 0.1f);
        chartLayout.addView(mChartView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void ServiceConnected() {
        super.ServiceConnected();

        switch (scope) {
            case WindSensor:
            case PressureSensor:
            case RainSensor:
                publish("cha/hub/getlog", "5in1_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
                break;
            case WaterLevelSensor:
                publish("cha/hub/getlog", "tank_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
                break;
        }
    }

    @Override
    public void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        Sensor5in1Data data;
        int value1 = 0;
        String value2 = "";
        int id;
        long lastSync;

        switch (dataType) {
            case Sensor5in1StateW:
                id = intent.getIntExtra("id", -1);
                if (id != widgetId)
                    return;

                switch (scope) {
                    case WindSensor:
                        data = OtherControllerData.Instance.get5in1SensorData();
                        value1 = data.getWindSpeed();
                        value2 = String.format(Locale.US, "%d °", data.getWindDirection());
                        lastSync = data.getLastSyncTime();
                        break;

                    case RainSensor:
                        data = OtherControllerData.Instance.get5in1SensorData();
                        value1 = data.getRain();
                        lastSync = data.getLastSyncTime();
                        break;

                    case PressureSensor:
                        data = OtherControllerData.Instance.get5in1SensorData();
                        value1 = data.getPressure();
                        lastSync = data.getLastSyncTime();
                        break;

                    case WaterLevelSensor:
                        WaterLevelData wd = OtherControllerData.Instance.getWaterLevelData(id);
                        value1 = wd.getWaterPercent();
                        int x = wd.getWaterDistance();
                        if (x == Utils.I_UNDEFINED)
                            value2 = String.format(Locale.US, "-- cm %s %s", wd.getFloatSwitchIsOn() ? "F" : "", wd.getSolenoidIsOn() ? "S" : "");
                        else
                            value2 = String.format(Locale.US, "%d cm %s %s", x, wd.getFloatSwitchIsOn() ? "F" : "", wd.getSolenoidIsOn() ? "S" : "");
                        lastSync = wd.getLastSyncTime();
                        break;

                    default:
                        return;
                }

                LogTwoValueItem point = new LogTwoValueItem(new Date(lastSync), value1, value2);
                logBuffer.add(point);
                adapter.notifyDataSetChanged();

                xyDataSet.getSeriesAt(0).add(lastSync, value1);
                mChartView.repaint();
                break;

            case Log:
                switch (scope) {
                    case WindSensor:
                    case PressureSensor:
                    case RainSensor:
                        if (intent.getStringExtra("type").startsWith("5in1")) {
                            String log = intent.getStringExtra("log");
                            ThermostatUtils.Fill5in1SensorLog(scope, log, logBuffer);
                            adapter.notifyDataSetChanged();
                            ThermostatUtils.DrawTwoValueChart(logBuffer, mChartView, mRenderer, xyDataSet);
                        }
                        break;

                    case WaterLevelSensor:
                        if (intent.getStringExtra("type").startsWith("tank")) {
                            String log = intent.getStringExtra("log");
                            ThermostatUtils.FillWaterLevelLog(scope, log, logBuffer);
                            adapter.notifyDataSetChanged();
                            ThermostatUtils.DrawTwoValueChart(logBuffer, mChartView, mRenderer, xyDataSet);
                        }
                        break;
                }
                break;
        }
    }

    public class _5in1LogAdapter extends ArrayAdapter<LogTwoValueItem> {

        private final boolean has2ndValue;

        _5in1LogAdapter(Context context, ArrayList<LogTwoValueItem> points, boolean has2ndValue) {
            super(context, 0, points);

            this.has2ndValue = has2ndValue;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {


            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_key_value, parent, false);
            }

            LogTwoValueItem point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText(sdf.format(point.date));
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue1)).setText(String.format(Locale.US, "%d", point.Value1));
                if (has2ndValue)
                    ((TextView) convertView.findViewById(R.id.tvListViewItemValue2)).setText(point.Value2);
                else
                    convertView.findViewById(R.id.tvListViewItemValue2).setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);

        int id = 0;
        int logSuffix = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        switch (logSuffix) {
            case 0:
                id = R.id.action_sunday;
                break;
            case 1:
                id = R.id.action_monday;
                break;
            case 2:
                id = R.id.action_tuesday;
                break;
            case 3:
                id = R.id.action_wednesday;
                break;
            case 4:
                id = R.id.action_thursday;
                break;
            case 5:
                id = R.id.action_friday;
                break;
            case 6:
                id = R.id.action_saturday;
                break;
        }
        if (id > 0)
            menu.findItem(id).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        item.setChecked(!item.isChecked());

        int wd = -1;
        switch (id) {
            case R.id.action_sunday:
                wd = 0;
                break;
            case R.id.action_monday:
                wd = 1;
                break;
            case R.id.action_tuesday:
                wd = 2;
                break;
            case R.id.action_wednesday:
                wd = 3;
                break;
            case R.id.action_thursday:
                wd = 4;
                break;
            case R.id.action_friday:
                wd = 5;
                break;
            case R.id.action_saturday:
                wd = 6;
                break;
        }

        if (wd >= 0) {
            switch (scope) {
                case WindSensor:
                case PressureSensor:
                case RainSensor:
                    publish("cha/hub/getlog", "5in1_".concat(String.valueOf(wd)), false);
                    break;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
