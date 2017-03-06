package ge.altasoft.gia.cha.classes;

import java.util.Date;

public class LogTHItem {
    public final Date date;
    public final float T;
    public final float H;

    public LogTHItem(Date date, float T, float H) {
        this.date = date;
        this.T = T;
        this.H = H;
    }
}
