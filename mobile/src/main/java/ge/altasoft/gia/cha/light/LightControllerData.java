package ge.altasoft.gia.cha.light;

import android.content.SharedPreferences;
import android.util.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import ge.altasoft.gia.cha.RelayControllerData;


public final class LightControllerData extends RelayControllerData {

    public final static int RELAY_COUNT = 12;

    public final static LightControllerData Instance = new LightControllerData();

    private short sunriseMin;
    private short sunsetMin;

    private LightControllerData() {
        super();

        sunriseMin = 8 * 60;
        sunsetMin = 22 * 60;
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


    String encodeState() {
        StringBuilder sb = new StringBuilder();

        sb.append('$');
        sb.append(isActive() ? 'T' : 'F');

        if (haveSettings()) {
            for (int i = 0; i < RELAY_COUNT; i++)
                relays(i).encodeState(sb);
        } else {
            for (int i = 0; i < RELAY_COUNT; i++)
                sb.append('0');
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
        sb.append(sdf.format(getNow()));
        sb.append(String.format(Locale.US, "%04X%04X", sunriseMin, sunsetMin));
        return sb.toString();
    }

    public String encodeSettings() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        sb.append('*');
        sb.append(isActive() ? 'T' : 'F');

        for (int i = 0; i < RELAY_COUNT; i++) {
            if (haveSettings())
                relays(i).encodeSettings(sb);
            else LightRelayData.encodeSettingsDebug(sb);
        }

        sb.append('*');
        for (int i = 0; i < RELAY_COUNT; i++) {
            if (haveSettings())
                relays(i).encodeOrderAndName(sb2);
            else
                LightRelayData.encodeOrderAndNameDebug(sb2, i);
        }
        sb2.insert(0, String.format(Locale.US, "%04X", sb2.length()));
        sb.append(sb2);

        return sb.toString();
    }

    public boolean decode(String response, IDrawRelaysUI drawUI) {

        if (response == null) return false;

        Log.d("decode light", response);

        if ((response.charAt(0) != '$') && (response.charAt(0) != '*')) {
            Log.e("LightControllerData", "Not '$' or '*'");
            return false;
        }

        if (response.charAt(0) == '$') {
            if (!haveSettings())
                return false;

            setIsActive(response.charAt(1) != 'F');

            for (int i = 0; i < RELAY_COUNT; i++)
                relays(i).setIsOn(response.charAt(i + 2) == '1');

            int idx = RELAY_COUNT + 2;
            setNow(response.substring(idx, idx + 12));
            sunriseMin = Short.parseShort(response.substring(idx + 12, idx + 16), 16);
            sunsetMin = Short.parseShort(response.substring(idx + 16, idx + 20), 16);

            drawUI.drawFooterRelays();

            return false;
        }

        setIsActive(response.charAt(1) != 'F');

        int idx = 2;
        for (int i = 0; i < RELAY_COUNT; i++) {
            LightRelayData relay = new LightRelayData(i + 1);
            idx = relay.decodeSettings(response, idx);
            setRelay(i, relay);
        }

        setHaveSettings(true);

        if (response.charAt(idx) != '*') {
            Log.e("LightControllerData", "Not '*'");
            return false;
        }
        //int length = Integer.parseInt(response.substring(idx + 1, idx + 5), 16);

        response = response.substring(idx + 5);

        String[] arr = response.split("\\$"); // maybe there's also state data
        response = arr[0];
        String stateResponse = null;
        if (arr.length == 2)
            stateResponse = '$' + arr[1];

        arr = response.split(";");
        if (arr.length != RELAY_COUNT) {
            Log.e("LightControllerData", "Invalid number of relays returned");
            return false;
        }

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).decodeOrderAndName(arr[i]);

        rebuildUI(drawUI);

        if (stateResponse != null)
            decode(stateResponse, drawUI);

        return true;
    }

    public void decode(SharedPreferences prefs) {

        setIsActive(prefs.getBoolean("l_automatic_mode", false));

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).decodeSettings(prefs);
    }

    public void saveToPreferences(SharedPreferences prefs) {
        LightControllerData ss = LightControllerData.Instance;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("l_automatic_mode", ss.isActive());

        for (int i = 0; i < RELAY_COUNT; i++)
            relays(i).encodeSettings(editor);

        editor.apply();
    }

    String GetStatusText() {
        String now = DateFormat.getDateTimeInstance().format(this.getNow());
        //SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
        //sdf.format(getNow)
        return String.format(Locale.US, "%s, Sunrise: %d:%02d, Sunset: %d:%02d", now, sunriseMin / 60, sunriseMin % 60, sunsetMin / 60, sunsetMin % 60);
    }
}


