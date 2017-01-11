package ge.altasoft.gia.cha.thermostat;

import ge.altasoft.gia.cha.RelayData;
import ge.altasoft.gia.cha.views.BoilerPumpView;

public final class BoilerPumpData extends RelayData {

    private BoilerPumpView pumpView;

    public BoilerPumpData(int id) {
        super(id);
    }

    public void setIsOn(boolean value) {
        super._setIsOn(value);
        if (pumpView != null)
            pumpView.setIsOn(value);
    }

    public void setBoilerPumpView(BoilerPumpView relayView) {
        this.pumpView = relayView;
    }
}
