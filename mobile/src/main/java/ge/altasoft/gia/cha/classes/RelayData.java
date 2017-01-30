package ge.altasoft.gia.cha.classes;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.Utils;

public abstract class RelayData implements Comparable<RelayData> {

    final private int id;
    private int order;
    private boolean isOn;

    private String name;

    final private CircularArrayList<Pair<Date, Boolean>> logBuffer = new CircularArrayList<>(Utils.LOG_BUFFER_SIZE);

    protected RelayData(int id) {
        this.id = id;
        this.order = id;
        this.isOn = false;
        this.name = "Default Relay #" + String.valueOf(id);
    }

    public CircularArrayList<Pair<Date, Boolean>> getLogBuffer() {
        return logBuffer;
    }

    public int getId() {
        return this.id;
    }

    public int getOrder() {
        return this.order;
    }

    public boolean isOn() {
        return this.isOn;
    }

    public String getName() {
        return this.name;
    }

    public void setIsOn(boolean value) {
        if (this.isOn != value) {
            this.isOn = value;
            logBuffer.add(new Pair<>(new Date(), value));
        }
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

    @Override
    public int compareTo(@NonNull RelayData o) {
        if (Integer.valueOf(this.order).equals(o.order)) {
            return Integer.valueOf(this.id).compareTo(o.id);
        } else {
            return Integer.valueOf(this.order).compareTo(o.order);
        }
    }

    public void decodeState(String payload) {
        boolean value = !payload.equals("0");
        setIsOn(value);
    }
}
