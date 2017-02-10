package ge.altasoft.gia.cha;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.Calendar;
import java.util.Date;

import ge.altasoft.gia.cha.thermostat.BoilerSensorData;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;
import ge.altasoft.gia.cha.thermostat.ThermostatUtils;

public class GraphActivity extends ChaActivity {

    private Menu mainMenu;

    private GraphicalView mChartView;
    private XYMultipleSeriesDataset xyDataSet = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        LinearLayout chartLayout = (LinearLayout) findViewById(R.id.chartBig);
        XYSeries series1 = new XYSeries("T1");
        XYSeries series2 = new XYSeries("T2");
        XYSeries series3 = new XYSeries("T3");

        mRenderer = ThermostatUtils.getBoilerSensorChartRenderer();
        mRenderer.setZoomEnabled(true, true);
        mRenderer.setPanEnabled(true, true);
        mRenderer.setZoomButtonsVisible(true);
        //mRenderer.setPanLimits(new double[] { -10, 20, -10, 40 });
        //mRenderer.setZoomLimits(new double[] { -10, 20, -10, 40 });

        xyDataSet.addSeries(series1);
        xyDataSet.addSeries(series2);
        xyDataSet.addSeries(series3);

        mChartView = ChartFactory.getCubeLineChartView(this, xyDataSet, mRenderer, 0.1f);
        chartLayout.addView(mChartView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        switch (dataType) {
            case ThermostatBoilerSensorState:
                int id = intent.getIntExtra("id", 0);
                id--;

                BoilerSensorData data = ThermostatControllerData.Instance.boilerSensors(id);
                float v = data.getTemperature();
                if (!Float.isNaN(v)) {
                    xyDataSet.getSeriesAt(id).add(data.getLastReadingTime(), v);
                    mChartView.repaint();
                }
                break;

            case ThermostatLog:
                if (intent.getStringExtra("type").startsWith("boiler"))
                    rebuildGraph(intent.getStringExtra("log"));
                break;
        }
    }

    @Override
    protected void ServiceConnected() {
        super.ServiceConnected();

        publish("cha/hub/getlog", "boiler_".concat(String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)), false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph, menu);

        this.mainMenu = menu;

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

        if (wd > 0) {
            publish("cha/hub/getlog", "boiler_".concat(String.valueOf(wd)), false);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    public void rebuildGraph(String log) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDt = calendar.getTime();

        ThermostatUtils.DrawBoilerSensorChart(log, mChartView, mRenderer, xyDataSet, startDt, 60);
    }
}
