package ge.altasoft.gia.cha.light;

import android.content.SharedPreferences;

import java.util.Locale;

import ge.altasoft.gia.cha.classes.RelayData;
import ge.altasoft.gia.cha.Utils;

public final class LightRelayData extends RelayData {

    private static final char OFF_SUNRISE = 'S';
    private static final char OFF_TIME = 'T';
    private static final char OFF_DURATION = 'D';

    private boolean isActive;
    private short onOffset;
    private String offMode;
    private short offValue;

    LightRelayData(int id) {
        super(id);

        this.isActive = false;
        this.onOffset = 0;
        this.offMode = String.valueOf(OFF_SUNRISE);
        this.offValue = 0;
    }

    private String getOnTime() {
        int minutes = LightControllerData.Instance.getSunsetMin() + onOffset;

        int hours = minutes / 60;
        minutes = minutes % 60;

        return String.format(Locale.US, "%d:%02d", hours, minutes);
    }

    public String getComment() {
        if (isActive)
            return getOnTime() + " - " + getOffTime();
        else
            return "";
    }

    private String getOffTime() {
        int minutes = 0;
        switch (offMode.charAt(0)) {
            case OFF_SUNRISE:
                minutes = LightControllerData.Instance.getSunriseMin() + offValue;
                break;
            case OFF_TIME:
                minutes = offValue;
                break;
            case OFF_DURATION:
                minutes = LightControllerData.Instance.getSunsetMin() + onOffset + offValue;
                break;
        }

        int hours = minutes / 60;
        minutes = minutes % 60;

        return String.format(Locale.US, "%d:%02d", hours, minutes);
    }

    public int decodeSettings(String response, int idx) {
        isActive = response.charAt(idx) != 'F';
        onOffset = Short.parseShort(response.substring(idx + 1, idx + 5), 16);
        offMode = response.substring(idx + 5, idx + 6);
        offValue = Short.parseShort(response.substring(idx + 6, idx + 10), 16);

        idx += 10;
        return idx;
    }

    void decodeSettings(SharedPreferences prefs) {
        String suffix = Integer.toString(getId());

        isActive = prefs.getBoolean("l_is_active_" + suffix, false);
        onOffset = (short) prefs.getInt("l_on_offset_" + suffix, 0);
        offMode = prefs.getString("l_off_mode_" + suffix, "S");

        short value = (short) prefs.getInt("l_off_value_" + suffix, 0);
        if ((value < 0) && (offMode.equals("T") || offMode.equals("D"))) // positive values only
            value = 0;
        offValue = value;

        setName(prefs.getString("l_relay_name_" + suffix, "Relay #" + suffix));
    }

    public void encodeSettings(StringBuilder sb) {
        sb.append(isActive ? 'T' : 'F');
        sb.append(Utils.shortToHex4(onOffset));
        sb.append(offMode);
        sb.append(Utils.shortToHex4(offValue));
    }

    public static void encodeSettingsDebug(StringBuilder sb) {
        sb.append('F');
        sb.append(Utils.shortToHex4((short) 0));
        sb.append(LightRelayData.OFF_SUNRISE);
        sb.append(Utils.shortToHex4((short) 0));
    }

    void encodeSettings(SharedPreferences.Editor editor) {
        String suffix = Integer.toString(getId());

        editor.putBoolean("l_is_active_" + suffix, isActive);
        editor.putInt("l_on_offset_" + suffix, onOffset);
        editor.putString("l_off_mode_" + suffix, offMode);
        editor.putInt("l_off_value_" + suffix, offValue);
        editor.putString("l_relay_name_" + suffix, getName());
    }
}