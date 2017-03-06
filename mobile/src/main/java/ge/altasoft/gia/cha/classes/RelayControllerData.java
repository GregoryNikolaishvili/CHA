package ge.altasoft.gia.cha.classes;

import android.util.Log;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public abstract class RelayControllerData {

    private boolean isActive;
    private boolean haveSettings;

    final private RelayData[] relayDatas;
    final private int[] savedRelayOrders;
    protected boolean widgetsReordered;

    protected abstract int relayCount();

    protected RelayControllerData() {
        isActive = false;
        widgetsReordered = false;
        haveSettings = false;
        relayDatas = new RelayData[relayCount()];
        savedRelayOrders = new int[relayCount()];
    }

    public boolean haveSettings() {
        return haveSettings;
    }

    public RelayData relays(int index) {
        return relayDatas[index];
    }

    public RelayData[] sortedRelays() {
        RelayData[] r = Arrays.copyOf(relayDatas, relayDatas.length);
        Arrays.sort(r);
        return r;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean widgetOrderChanged() {
        return widgetsReordered;
    }

    protected void setRelay(int index, RelayData relay) {
        relayDatas[index] = relay;
    }

    protected void setIsActive(boolean value) {
        this.isActive = value;
    }

    protected void setHaveSettings() {
        this.haveSettings = true;
    }

    public void saveWidgetOrders() {
        widgetsReordered = false;
        for (int i = 0; i < relayDatas.length; i++)
            savedRelayOrders[i] = relayDatas[i].getOrder();
    }

    public void restoreWidgetOrders() {
        for (int i = 0; i < relayDatas.length; i++)
            relayDatas[i].setOrder(savedRelayOrders[i]);
        widgetsReordered = false;
    }

    public RelayData getRelayFromUIIndex(int index) {
        RelayData[] r = Arrays.copyOf(relayDatas, relayDatas.length);
        Arrays.sort(r);

        return r[index];
    }

    // TODO: 3/7/2017  
    public void reorderRelayMapping(int firstIndex, int secondIndex) {

        RelayData firstRelay = getRelayFromUIIndex(firstIndex);
        RelayData secondRelay = getRelayFromUIIndex(secondIndex);

        int order = firstRelay.getOrder();

        firstRelay.setOrder(secondRelay.getOrder());
        secondRelay.setOrder(order);

        widgetsReordered = true;
    }

    protected static <K, V extends Comparable<V>> Map<K, V> sortByOrder(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = -map.get(k2).compareTo(map.get(k1));
                if (compare == 0) return -1;
                else return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    //region Encode/Decode
    public String encodeSettings() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < relayDatas.length; i++)
            relays(i).encodeSettings(sb);

        return sb.toString();
    }

    public String encodeNamesAndOrder() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < relayDatas.length; i++)
            relays(i).encodeOrderAndName(sb);
        sb.insert(0, String.format(Locale.US, "%04X", sb.length()));

        return sb.toString();
    }

    public void decodeSettings(String response) {
        Log.d("decode relay settings", response);

        setIsActive(response.charAt(0) != 'F');

        int idx = 1;
        for (int i = 0; i < relayDatas.length; i++)
            idx = relays(i).decodeSettings(response, idx);

        setHaveSettings();
    }

    public void decodeNamesAndOrder(String response) {
        Log.d("decode relay names", response);

        response = response.substring(4); // first 4 digits is length in hex

        String[] arr = response.split(";");
        if (arr.length != relayDatas.length) {
            Log.e("LightControllerData", "Invalid number of relays returned");
            return;
        }

        for (int i = 0; i < relayDatas.length; i++)
            relays(i).decodeOrderAndName(arr[i]);
    }
    //endregion
}
