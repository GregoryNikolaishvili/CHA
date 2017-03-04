package ge.altasoft.gia.cha.classes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class DashboardItems {

    private final static ArrayList<DashboardItem> dashboardItems = new ArrayList<>();
    //private static ArrayList<DashboardItem> savedDashboardItems;
    private static boolean widgetsReordered;

    public static boolean hasItem(WidgetType type, int id) {
        for (DashboardItem item : dashboardItems) {
            if ((item.type == type) && (item.id == id))
                return true;
        }
        return false;
    }

    public static void add(Context context, WidgetType type, int id) {
        if (hasItem(type, id))
            return;

        dashboardItems.add(new DashboardItem(type, id));

        notifyChanges(context);
        saveToPreferences(context);
    }

    public static void add(WidgetType type, int id) {
        if (hasItem(type, id))
            return;

        dashboardItems.add(new DashboardItem(type, id));
    }

    public static void clear() {
        dashboardItems.clear();
    }

    private static void notifyChanges(Context context) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ge.altasoft.gia.DASH_CHANGED");
        context.sendBroadcast(broadcastIntent);
    }

    public static void remove(Context context, WidgetType type, int id) {
        for (DashboardItem item : dashboardItems) {
            if ((item.type == type) && (item.id == id)) {
                dashboardItems.remove(item);
                break;
            }
        }

        notifyChanges(context);
        saveToPreferences(context);
    }

    public static int size() {
        return dashboardItems.size();
    }

    public static DashboardItem getItemAt(int position) {
        return dashboardItems.get(position);
    }

    public static void saveToPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        StringBuilder sb = new StringBuilder();

        for (DashboardItem item : dashboardItems) {
            sb.append(item.type.name()).append(':').append(item.id).append(";");
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("dashboard_items", sb.toString());
        editor.apply();
    }

    public static void restoreFromPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String saved = prefs.getString("dashboard_items", null);
        if (saved == null)
            return;

        dashboardItems.clear();
        String[] items = saved.split(";");
        for (int i = 0; i < items.length; i++) {
            String[] parts = items[i].split(":");

            WidgetType wt;
            if (Character.isDigit(parts[0].charAt(0)))
                wt = WidgetType.fromInt(Integer.parseInt(parts[0]));
            else
                wt = WidgetType.valueOf(parts[0]);

            dashboardItems.add(new DashboardItem(wt, Integer.valueOf(parts[1])));
        }
    }

    public static void reorderMapping(int firstIndex, int secondIndex) {

        //Collections.swap(dashboardItems, firstIndex, secondIndex);
        widgetsReordered = true;
    }

    public static boolean widgetOrderChanged() {

        return widgetsReordered;
    }

    public static void saveWidgetOrders() {
        widgetsReordered = false;
//        savedDashboardItems = new ArrayList<>();
//        for (DashboardItem item : dashboardItems)
//            savedDashboardItems.add(new DashboardItem(item.type, item.id));
    }

    public static void restoreWidgetOrders(Context context) {

//        dashboardItems.clear();
//        for (DashboardItem item : savedDashboardItems)
//            dashboardItems.add(new DashboardItem(item.type, item.id));
//        widgetsReordered = false;
        notifyChanges(context);
    }
}


