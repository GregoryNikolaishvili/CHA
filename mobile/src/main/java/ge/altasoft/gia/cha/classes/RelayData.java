package ge.altasoft.gia.cha.classes;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.Utils;

public abstract class RelayData implements Comparable<RelayData> {

    final private int id;
    private int order;
    private int state;

    private String name;
    private long lastSyncTime;

    protected RelayData(int id) {
        this.id = id;
        this.order = id;
        this.state = 0;
        this.name = "Default Relay #" + String.valueOf(id);
    }

    public long getLastSyncTime() {
        return this.lastSyncTime;
    }

    public int getId() {
        return this.id;
    }

    public int getState() {
        return this.state;
    }

    public String getName() {
        return this.name;
    }

    private void setLastSyncTime() {
        lastSyncTime = new Date().getTime();
    }
    private void setState(int value) {
        this.state = value;
        setLastSyncTime();
    }

    public void setName(String value) {
        this.name = value;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    void decodeOrderAndName(String s) {
        order = Character.digit(s.charAt(0), 16);
        name = Utils.decodeArduinoString(s.substring(1));
        if (name.equals(""))
            name = "Relay #" + order;
    }

    public void encodeSettings(StringBuilder sb) {
    }

    public int decodeSettings(String response, int idx) {
        return idx;
    }

    void encodeOrderAndName(StringBuilder sb2) {
        sb2.append(String.format(Locale.US, "%01X", order));
        sb2.append(Utils.encodeArduinoString(name));
        sb2.append(';');
    }

    public void decodeState(String payload) {
        setState(Integer.parseInt(payload));
    }

    @Override
    public int compareTo(@NonNull RelayData o) {
        if (Integer.valueOf(this.order).equals(o.order)) {
            return Integer.valueOf(this.id).compareTo(o.id);
        } else {
            return Integer.valueOf(this.order).compareTo(o.order);
        }
    }
}
