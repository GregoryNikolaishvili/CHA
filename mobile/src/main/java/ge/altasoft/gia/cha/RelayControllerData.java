package ge.altasoft.gia.cha;

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
    private Date now;

    private RelayData[] relays;
    private int[] savedRelayOrders;
    private boolean relaysReordered;

    public abstract int relayCount();

    public RelayControllerData() {
        isActive = false;
        relaysReordered = false;
        haveSettings = false;
        now = new Date();
        relays = new RelayData[relayCount()];
        savedRelayOrders = new int[relayCount()];
    }

    public boolean haveSettings() {
        return haveSettings;
    }

    public RelayData relays(int index) {
        return relays[index];
    }

    public boolean isActive() {
        return isActive;
    }

    protected boolean relayOrderChanged() {
        return relaysReordered;
    }

    protected Date getNow() {
        return this.now;
    }

    protected void setNow(String dateAndTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss", Locale.US);
        try {
            now = sdf.parse(dateAndTime);
        } catch (ParseException ex) {
        }
    }


    protected void setRelay(int index, RelayData relay) {
        relays[index] = relay;
    }


    public void setIsActive(boolean value) {
        this.isActive = value;
    }

    protected void setHaveSettings(boolean value) {
        this.haveSettings = value;
    }


    protected void rebuildUI(IDrawRelaysUI drawUI) {
        drawUI.clearAllRelays();

        RelayData[] r = Arrays.copyOf(relays, relays.length);
        Arrays.sort(r);

        for (RelayData aR : r) drawUI.createNewRelay(aR);
    }

    protected void saveRelayOrders() {
        relaysReordered = false;
        for (int i = 0; i < relays.length; i++)
            savedRelayOrders[i] = relays[i].getOrder();
    }

    protected void restoreRelayOrders() {
        for (int i = 0; i < relays.length; i++)
            relays[i].setOrder(savedRelayOrders[i]);
        relaysReordered = false;
    }

    private RelayData getRelayFromUIIndex(int index) {
        RelayData[] r = Arrays.copyOf(relays, relays.length);
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

    public interface IDrawRelaysUI {
        void createNewRelay(final RelayData relayData);

        void clearAllRelays();

        void drawFooterRelays();
    }
}
