package ge.altasoft.gia.cha.classes;

import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.Date;
import java.util.Locale;

import ge.altasoft.gia.cha.Utils;

public abstract class RelayData implements Comparable<RelayData> {

    private int id;
    private int order;
    private boolean isOn;

    private String name;

    private CircularArrayList<Pair<Date, Boolean>> logBuffer = new CircularArrayList<>(Utils.LOG_BUFFER_SIZE);

    public RelayData(int id) {
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
            logBuffer.add(new Pair<Date, Boolean>(new Date(), value));
        }
    }

    public void setName(String value) {
        this.name = value;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void decodeOrderAndName(String s) {
        order = Character.digit(s.charAt(0), 16);
        name = Utils.DecodeArduinoString(s.substring(1));
        if (name.equals(""))
            name = "Relay #" + order;
    }

    public void encodeState(StringBuilder sb) {
        sb.append(isOn ? '1' : '0');
    }

    public void encodeSettings(StringBuilder sb) {
    }

    public static void encodeSettingsDebug(StringBuilder ignored) {
    }

    public int decodeSettings(String response, int idx) {
        return idx;
    }

    public void encodeOrderAndName(StringBuilder sb2) {
        sb2.append(String.format(Locale.US, "%01X", order));
        sb2.append(Utils.EncodeArduinoString(name));
        sb2.append(';');
    }

    public static void encodeOrderAndNameDebug(StringBuilder sb2, int i) {
        sb2.append(String.format(Locale.US, "%01X", i));
        sb2.append(Utils.EncodeArduinoString("Relay #" + String.valueOf(i + 1)));
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
}
