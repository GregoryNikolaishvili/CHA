package ge.altasoft.gia.cha.classes;

import java.util.Date;

public class LogTwoValueItem {
    public final Date date;
    public final int Value1;
    public final String Value2;

    public LogTwoValueItem(Date date, int value1, String value2) {
        this.date = date;
        this.Value1 = value1;
        this.Value2 = value2;
    }
}
