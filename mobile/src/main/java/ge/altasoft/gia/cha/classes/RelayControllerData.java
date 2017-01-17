package ge.altasoft.gia.cha.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public abstract class RelayControllerData {

    private boolean isActive;
    private boolean haveSettings;
    private Date controllerCurrentTime;

    private RelayData[] relayDatas;
    private int[] savedRelayOrders;
    private boolean relaysReordered;

    public abstract int relayCount();

    public RelayControllerData() {
        isActive = false;
        relaysReordered = false;
        haveSettings = false;
        this.controllerCurrentTime = new Date();
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

    public boolean relayOrderChanged() {
        return relaysReordered;
    }

    protected Date getControllerCurrentTime() {
        return this.controllerCurrentTime;
    }

    protected void setControllerCurrentTime(String dateAndTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
        try {
            this.controllerCurrentTime = sdf.parse(dateAndTime);
        } catch (ParseException ignored) {
        }
    }


    protected void setRelay(int index, RelayData relay) {
        relayDatas[index] = relay;
    }

    public void setIsActive(boolean value) {
        this.isActive = value;
    }

    protected void setHaveSettings(boolean value) {
        this.haveSettings = value;
    }

    public void saveRelayOrders() {
        relaysReordered = false;
        for (int i = 0; i < relayDatas.length; i++)
            savedRelayOrders[i] = relayDatas[i].getOrder();
    }

    public void restoreRelayOrders() {
        for (int i = 0; i < relayDatas.length; i++)
            relayDatas[i].setOrder(savedRelayOrders[i]);
        relaysReordered = false;
    }

    private RelayData getRelayFromUIIndex(int index) {
        RelayData[] r = Arrays.copyOf(relayDatas, relayDatas.length);
        Arrays.sort(r);

        return r[index];
    }

    public void reorderRelayMapping(int firstIndex, int secondIndex) {

        RelayData firstRelay = getRelayFromUIIndex(firstIndex);
        RelayData secondRelay = getRelayFromUIIndex(secondIndex);

        int order = firstRelay.getOrder();

        firstRelay.setOrder(secondRelay.getOrder());
        secondRelay.setOrder(order);

        relaysReordered = true;
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
}
