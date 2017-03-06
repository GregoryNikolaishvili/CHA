package ge.altasoft.gia.cha.classes;

import java.io.Serializable;

public class DashboardItem implements Serializable {

    public final WidgetType type;
    public final int id;

    DashboardItem(WidgetType type, int id) {
        this.type = type;
        this.id = id;
    }
}
