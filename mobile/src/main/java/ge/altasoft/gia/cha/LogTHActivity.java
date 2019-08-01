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

import ge.altasoft.gia.cha.classes.LogTHItem;
import ge.altasoft.gia.cha.classes.WidgetType;
import ge.altasoft.gia.cha.other.OtherControllerData;
import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class LogTHActivity extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private WidgetType scope;
    private int sensorId;

    private THLogAdapter adapter = null;
    private ArrayList<LogTHItem> logBuffer;

    private GraphicalView mChartView;
    private final XYMultipleSeriesDataset xyDataSet = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_th);

        Intent intent = getIntent();
        scope = (WidgetType) intent.getSerializableExtra("widget");
        sensorId = intent.getIntExtra("id", -1);

        logBuffer = new ArrayList<>();
        adapter = new THLogAdapter(this, logBuffer, scope == WidgetType.RoomSensor);

        ListView listView = (ListView) findViewById(R.id.lvLog);
        listView.setAdapter(adapter);

        LinearLayout chartLayout = (LinearLayout) findViewById(R.id.chartLogBig);
        XYSeries seriesT = new XYSeries("T");

        mRenderer = ThermostatUtils.getSensorChartRenderer(this, false, 1, new int[]{Color.RED});
        mRenderer.setZoomEnabled(true, true);
        mRenderer.setPanEnabled(true, true);
        mRenderer.setZoomButtonsVisible(true);
        mRenderer.setShowLegend(true);

        //mRenderer.setPanLimits(new double[] { -10, 20, -10, 40 });
        //mRenderer.setZoomLimits(new double[] { -10, 20, -10, 40 });

        xyDataSet.addSeries(seriesT);

        mChartView = ChartFactory.getCubeLineChartView(this, xyDataSet, mRenderer, 0.1f);
        chartLayout.addView(mChartView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void ServiceConnected() {
        super.ServiceConnected();

        switch (scope) {
            case BoilerSensor:
                publish("cha/hub/getlog", "boiler_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
                break;
            case RoomSensor:
                publish("cha/hub/getlog", "room_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
                break;
        }
    }

    @Override
    public void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        int id;
        float v;

        switch (dataType) {
            case ThermostatBoilerSensorState:
                if (scope != WidgetType.BoilerSensor)
                    return;
                id = intent.getIntExtra("id", -1);
                if (id != sensorId)
                    return;

                BoilerSensorData bsdata = ThermostatControllerData.Instance.boilerSensors(id);
                v = bsdata.getTemperature();
                if (!Float.isNaN(v)) {
                    LogTHItem point = new LogTHItem(new Date(bsdata.getLastSyncTime()), v, 0f);
                    logBuffer.add(point);
                    adapter.notifyDataSetChanged();

                    xyDataSet.getSeriesAt(0).add(bsdata.getLastSyncTime(), v);
                    mChartView.repaint();
                }
                break;

            case SensorRoomState:
                if (scope != WidgetType.RoomSensor)
                    return;
                id = intent.getIntExtra("id", -1);
                if (id != sensorId)
                    return;

                RoomSensorData rsd = ThermostatControllerData.Instance.roomSensors(id, false);

                v = rsd.getTemperature();
                if (!Float.isNaN(v)) {
                    LogTHItem point = new LogTHItem(new Date(rsd.getLastSyncTime()), v, 0f);
                    logBuffer.add(point);
                    adapter.notifyDataSetChanged();

                    xyDataSet.getSeriesAt(0).add(rsd.getLastSyncTime(), v);
                    mChartView.repaint();
                }
                break;

            case Log:
                switch (scope) {
                    case BoilerSensor:
                        if (intent.getStringExtra("type").startsWith("boiler")) {
                            String log = intent.getStringExtra("log");
                            ThermostatUtils.FillTHSensorLog(sensorId, scope, log, logBuffer);
                            adapter.notifyDataSetChanged();
                            ThermostatUtils.DrawTHSensorChart(logBuffer, mChartView, mRenderer, xyDataSet);
                        }
                        break;
                    case RoomSensor:
                        if (intent.getStringExtra("type").startsWith("room")) {
                            String log = intent.getStringExtra("log");
                            ThermostatUtils.FillTHSensorLog(sensorId, scope, log, logBuffer);
                            adapter.notifyDataSetChanged();
                            ThermostatUtils.DrawTHSensorChart(logBuffer, mChartView, mRenderer, xyDataSet);
                        }
                        break;
                }
                break;
        }
    }

    class THLogAdapter extends ArrayAdapter<LogTHItem> {

        private final boolean hasHumidity;

        THLogAdapter(Context context, ArrayList<LogTHItem> points, boolean hasHumidity) {
            super(context, 0, points);

            this.hasHumidity = hasHumidity;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_key_value, parent, false);
            }

            LogTHItem point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText(sdf.format(point.date));
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue1)).setText(String.format(Locale.US, "%.1f°", point.T));

                if (hasHumidity)
                    ((TextView) convertView.findViewById(R.id.tvListViewItemValue2)).setText(String.format(Locale.US, "%.0f %%", point.H));
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
                case BoilerSensor:
                    publish("cha/hub/getlog", "boiler_".concat(String.valueOf(wd)), false);
                    break;
                case RoomSensor:
                    publish("cha/hub/getlog", "room_".concat(String.valueOf(wd)), false);
                    break;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
