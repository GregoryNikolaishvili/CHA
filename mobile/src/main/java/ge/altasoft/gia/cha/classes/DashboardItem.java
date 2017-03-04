package ge.altasoft.gia.cha.classes;

import java.io.Serializable;

public class DashboardItem implements Serializable {

    public WidgetType type;
    public int id;

    DashboardItem(WidgetType type, int id) {
        this.type = type;
        this.id = id;
    }
}
