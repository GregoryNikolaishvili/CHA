package ge.altasoft.gia.cha.classes;

import java.io.Serializable;

public class DashboardItem implements Serializable {
    public int type;
    public int id;

    DashboardItem(int type, int id) {
        this.type = type;
        this.id = id;
    }
}
