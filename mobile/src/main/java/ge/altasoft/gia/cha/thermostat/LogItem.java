package ge.altasoft.gia.cha.thermostat;

import java.util.Date;

public class LogItem {
    public Date date;
    public float T;
    public float H;

    public LogItem(Date date, float T, float H) {
        this.date = date;
        this.T = T;
        this.H = H;
    }
}
