package ge.altasoft.gia.cha.classes;

import java.util.Date;

public class LogTHItem {
    public Date date;
    public float T;
    public float H;

    public LogTHItem(Date date, float T, float H) {
        this.date = date;
        this.T = T;
        this.H = H;
    }
}
