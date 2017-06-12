package ge.altasoft.gia.cha.other;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.thermostat.RoomSensorData;

public class Sensor5in1Data extends RoomSensorData {

    private int windSpeed;
    private int maxWindSpeed;
    private String maxWindSpeedTime;
    private int windDirection;
    private int rain;
    private int dailyRain;
    private int pressure;

    private long lastRainWindPressureSyncTime;

    Sensor5in1Data(int id) {
        super(id);
        this.windSpeed = 0;
        this.maxWindSpeed = 0;
        this.windDirection = 0;
        this.pressure = 0;
    }

    public long getRainWindPressureSyncTime() {
        return this.lastRainWindPressureSyncTime;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public int getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public String getMaxWindSpeedTime() {
        return maxWindSpeedTime;
    }

    public int getWindDirection() {
        return windDirection;
    }

    public int getPressure() {
        return pressure;
    }

    public int getRain() {
        return rain;
    }

    public int getDailyRain() {
        return dailyRain;
    }

    public void decodeState(String payload, boolean isWeatherData) {

        if (!isWeatherData) {
            super.decodeState(payload);
            return;
        }

        JSONObject jMain;
        try {
            jMain = new JSONObject(payload);
            this.windSpeed = jMain.getInt("WS");
            maxWindSpeed = jMain.getInt("WSM");
            maxWindSpeedTime = jMain.getString("WSMT");
            maxWindSpeedTime = maxWindSpeedTime.substring(0, 2).concat(":").concat(maxWindSpeedTime.substring(2, 4));
            windDirection = jMain.getInt("WD");
            rain = jMain.getInt("RR");
            dailyRain = jMain.getInt("DR");
            pressure = jMain.getInt("PR");

            lastRainWindPressureSyncTime = Utils.DecodeTime(jMain.getString("TIME"));

        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
        }
    }

}