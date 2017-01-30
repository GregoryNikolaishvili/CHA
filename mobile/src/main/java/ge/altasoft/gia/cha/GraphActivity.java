package ge.altasoft.gia.cha;

import android.content.Intent;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;

import ge.altasoft.gia.cha.classes.LineSeriesArray;
import ge.altasoft.gia.cha.thermostat.ThermostatControllerData;

public class GraphActivity extends ChaActivity {

    final private LineSeriesArray pointSeries = new LineSeriesArray(ThermostatControllerData.BOILER_SENSOR_COUNT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        for (int i = 0; i < ThermostatControllerData.BOILER_SENSOR_COUNT; i++)
            pointSeries.getItem(i).fill(ThermostatControllerData.Instance.boilerSensors(i).getLogBuffer());

        final GraphView graph = (GraphView) findViewById(R.id.graphBig);
        pointSeries.addToGraph(graph);

        graph.getViewport().scrollToEnd();
    }


    @Override
    void processMqttData(MqttClient.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

        if (dataType == MqttClient.MQTTReceivedDataType.ThermostatBoilerSensorState) {
            int id = intent.getIntExtra("id", 0);
            id--;

            pointSeries.getItem(id).append(ThermostatControllerData.Instance.boilerSensors(id));
        }
    }
}
