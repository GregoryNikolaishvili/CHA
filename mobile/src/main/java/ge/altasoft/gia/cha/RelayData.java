package ge.altasoft.gia.cha;

import android.support.annotation.NonNull;

import java.util.Locale;

public abstract class RelayData implements Comparable<RelayData> {

    private int id;
    private int order;
    private boolean isOn;

    private String name;

    public RelayData(int id) {
        this.id = id;
        this.order = id;
        this.isOn = false;
        this.name = "Default Relay #" + String.valueOf(id);
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

    public void _setIsOn(boolean value) {
        this.isOn = value;
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


    public static void encodeSettingsDebug(StringBuilder sb) {
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
