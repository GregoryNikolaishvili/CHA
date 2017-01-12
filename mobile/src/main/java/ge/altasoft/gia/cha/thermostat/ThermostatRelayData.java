package ge.altasoft.gia.cha.thermostat;

import ge.altasoft.gia.cha.RelayData;
import ge.altasoft.gia.cha.views.ThermostatRelayView;

public final class ThermostatRelayData extends RelayData {

    private ThermostatRelayView relayView;

    ThermostatRelayData(int id) {
        super(id);
    }

    public String getComment() {
        return "";
    }

    void setIsOn(boolean value) {
        super._setIsOn(value);
        if (relayView != null)
            relayView.setIsOn(value);
    }

    public void setRelayView(ThermostatRelayView relayView) {
        this.relayView = relayView;
    }
}
