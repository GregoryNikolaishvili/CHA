package ge.altasoft.gia.cha.thermostat;

import android.content.SharedPreferences;

import ge.altasoft.gia.cha.classes.RelayData;

public final class ThermostatRelayData extends RelayData {

    ThermostatRelayData(int id) {
        super(id);
    }

    public String getComment() {
        return "";
    }

    void decodeSettings(SharedPreferences prefs) {
        String suffix = Integer.toString(getId());

        setName(prefs.getString("t_relay_name_" + suffix, "Relay #" + suffix));
    }

    void encodeSettings(SharedPreferences.Editor editor) {
        String suffix = Integer.toString(getId());

        editor.putString("t_relay_name_" + suffix, getName());
    }
}
