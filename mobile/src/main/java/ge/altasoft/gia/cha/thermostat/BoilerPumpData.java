package ge.altasoft.gia.cha.thermostat;

import ge.altasoft.gia.cha.RelayData;
import ge.altasoft.gia.cha.views.BoilerPumpView;

final class BoilerPumpData extends RelayData {

    private BoilerPumpView pumpView;

    BoilerPumpData(int id) {
        super(id);
    }

    void setIsOn(boolean value) {
        super._setIsOn(value);
        if (pumpView != null)
            pumpView.setIsOn(value);
    }

    void setBoilerPumpView(BoilerPumpView relayView) {
        this.pumpView = relayView;
    }
}
