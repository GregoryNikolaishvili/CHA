package ge.altasoft.gia.cha.light;

import android.content.SharedPreferences;

import java.text.DateFormat;

import ge.altasoft.gia.cha.classes.RelayControllerData;


public final class LightControllerData extends RelayControllerData {

    final static int RELAY_COUNT = 12;

    public final static LightControllerData Instance = new LightControllerData();

    private short sunriseMin;
    private short sunsetMin;

    private LightControllerData() {
        super();

        sunriseMin = 8 * 60;
        sunsetMin = 22 * 60;

        for (int i = 0; i < RELAY_COUNT; i++) {
            LightRelayData relay = new LightRelayData(i);
            setRelay(i, relay);
        }
    }

    @Override
    public int relayCount() {
        return RELAY_COUNT;
    }

    public LightRelayData relays(int index) {
        return (LightRelayData) super.relays(index);
    }

    short getSunriseMin() {
        return this.sunriseMin;
    }

    short getSunsetMin() {
        return this.sunsetMin;
    }

    String GetStatusText() {
        String now = DateFormat.getDateTimeInstance().format(this.getControllerCurrentTime());
        //return String.format(Locale.US, "%s, Sunrise: %d:%02d, Sunset: %d:%02d", now, sunriseMin / 60, sunriseMin % 60, sunsetMin / 60, sunsetMin % 60);
        return now;

    }

    //region Encode/Decode
    void decode(SharedPreferences prefs) {

        setIsActive(prefs.getBoolean("l_automatic_mode", false));

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).decodeSettings(prefs);
    }

    void saveToPreferences(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("l_automatic_mode", isActive());

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).encodeSettings(editor);

        editor.apply();
    }
    //endregion
}


