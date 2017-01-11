package ge.altasoft.gia.cha.light;

import android.content.SharedPreferences;

import java.util.Locale;

import ge.altasoft.gia.cha.RelayData;
import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.views.LightRelayView;

public final class LightRelayData extends RelayData {

    public static final char OFF_SUNRISE = 'S';
    public static final char OFF_TIME = 'T';
    public static final char OFF_DURATION = 'D';

    private LightRelayView relayView;

    private boolean isActive;
    private short onOffset;
    private String offMode;
    private short offValue;

    public LightRelayData(int id) {
        super(id);

        this.isActive = false;
        this.onOffset = 0;
        this.offMode = String.valueOf(OFF_SUNRISE);
        this.offValue = 0;
    }

    public void setIsOn(boolean value) {
        super._setIsOn(value);
        if (relayView != null)
            relayView.setIsOn(value);
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    private String getOnTime() {
        int minutes = LightControllerData.Instance.getSunriseMin() + onOffset;

        int hours = minutes / 60;
        minutes = minutes % 60;

        return String.format(Locale.US, "%d:%02d", hours, minutes);
    }

    public String getComment() {
        if (isActive)
            return "On: " + getOnTime() + ", Off: " + getOffTime();
        else
            return "";
    }

    public String getOffTime() {
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

    public void setRelayView(LightRelayView relayView) {
        this.relayView = relayView;
    }

    public int decodeSettings(String response, int idx) {

        isActive = response.charAt(idx) != 'F';
        onOffset = Short.parseShort(response.substring(idx + 1, idx + 5), 16);
        offMode = response.substring(idx + 5, idx + 6);
        offValue = Short.parseShort(response.substring(idx + 6, idx + 10), 16);

        idx += 10;
        return idx;
    }

    public void decodeSettings(SharedPreferences prefs) {
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
        sb.append(isActive() ? 'T' : 'F');
        sb.append(Utils.ShortToHex4(onOffset));
        sb.append(offMode);
        sb.append(Utils.ShortToHex4(offValue));
    }

    public static void encodeSettingsDebug(StringBuilder sb) {
        sb.append('F');
        sb.append(Utils.ShortToHex4((short) 0));
        sb.append(LightRelayData.OFF_SUNRISE);
        sb.append(Utils.ShortToHex4((short) 0));
    }

    public void encodeSettings(SharedPreferences.Editor editor) {
        String suffix = Integer.toString(getId());

        editor.putBoolean("l_is_active_" + suffix, isActive);
        editor.putInt("l_on_offset_" + suffix, onOffset);
        editor.putString("l_off_mode_" + suffix, offMode);
        editor.putInt("l_off_value_" + suffix, offValue);
        editor.putString("l_relay_name_" + suffix, getName());
    }
}