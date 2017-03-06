package ge.altasoft.gia.cha.classes;

import java.util.Date;

public class LogRelayItem {
    public final Date date;
    public final int state;

    public LogRelayItem(Date date, int state) {
        this.date = date;
        this.state = state;
    }
}
