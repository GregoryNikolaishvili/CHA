package ge.altasoft.gia.cha.other;

import android.content.SharedPreferences;

import ge.altasoft.gia.cha.Utils;
import ge.altasoft.gia.cha.classes.RelayData;

public final class PumpRelayData extends RelayData {

    private static final char OFF_SUNRISE = 'S';
    private static final char OFF_TIME = 'T';
    private static final char OFF_DURATION = 'D';

    private boolean isActive;
    private short onOffset;
    private String offMode;
    private short offValue;

    PumpRelayData(int id) {
        super(id);

        this.isActive = false;
        this.onOffset = 0;
        this.offMode = String.valueOf(OFF_SUNRISE);
        this.offValue = 0;
    }

    @Override
    public int decodeSettings(String response, int idx) {
        isActive = response.charAt(idx) != 'F';
        int value = Integer.parseInt(response.substring(idx + 1, idx + 5), 16);
        if (value > Short.MAX_VALUE)
            onOffset = (short)(0x10000 - value);
        else onOffset = (short)value;
        offMode = response.substring(idx + 5, idx + 6);
        offValue = Short.parseShort(response.substring(idx + 6, idx + 10), 16);

        idx += 10;
        return idx;
    }

    @Override
    public void encodeSettings(StringBuilder sb) {
        sb.append(isActive ? 'T' : 'F');
        sb.append(Utils.shortToHex4(onOffset));
        sb.append(offMode);
        sb.append(Utils.shortToHex4(offValue));
    }

    void decodeSettings(SharedPreferences prefs) {
        String suffix = Integer.toString(getId());

        isActive = prefs.getBoolean("wl_is_active_" + suffix, false);
        onOffset = (short) prefs.getInt("wl_on_offset_" + suffix, 0);
        offMode = prefs.getString("wl_off_mode_" + suffix, "S");

        short value = (short) prefs.getInt("wl_off_value_" + suffix, 0);
        if ((value < 0) && (offMode.equals("T") || offMode.equals("D"))) // positive values only
            value = 0;
        offValue = value;

        setName(prefs.getString("wl_relay_name_" + suffix, "Relay #" + suffix));
    }

    void encodeSettings(SharedPreferences.Editor editor) {
        String suffix = Integer.toString(getId());

        editor.putBoolean("wl_is_active_" + suffix, isActive);
        editor.putInt("wl_on_offset_" + suffix, onOffset);
        editor.putString("wl_off_mode_" + suffix, offMode);
        editor.putInt("wl_off_value_" + suffix, offValue);
        editor.putString("wl_relay_name_" + suffix, getName());
    }
}