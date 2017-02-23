package ge.altasoft.gia.cha.classes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;

import java.util.ArrayList;

public class DashboardItems {

    private final static ArrayList<DashboardItem> dashboardItems = new ArrayList<>();

    public static boolean hasItem(int type, int id) {
        for (DashboardItem item : dashboardItems) {
            if ((item.type == type) && (item.id == id))
                return true;
        }
        return false;
    }

    public static void add(Context context, int type, int id) {
        if (hasItem(type, id))
            return;

        dashboardItems.add(new DashboardItem(type, id));

        notifyChanges(context);
        save(context);
    }

    private static void notifyChanges(Context context) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ge.altasoft.gia.DASH_CHANGED");
        context.sendBroadcast(broadcastIntent);
    }

    public static void remove(Context context, int type, int id) {
        for (DashboardItem item : dashboardItems) {
            if ((item.type == type) && (item.id == id)) {
                dashboardItems.remove(item);
                break;
            }
        }

        notifyChanges(context);
        save(context);
    }

    public static int size() {
        return dashboardItems.size();
    }

    public static DashboardItem getItemAt(int position) {
        return dashboardItems.get(position);
    }

    public static void save(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        StringBuilder sb = new StringBuilder();

        for (DashboardItem item : dashboardItems) {
            sb.append(item.type).append(':').append(item.id).append(";");
        }


        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("dashboard_items", sb.toString());
        editor.apply();
    }

    public static void restore(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String saved = prefs.getString("dashboard_items", null);
        if (saved == null)
            return;

        dashboardItems.clear();
        String[] items = saved.split(";");
        for (int i = 0; i < items.length; i++) {
            String[] parts = items[i].split(":");

            dashboardItems.add(new DashboardItem(Integer.valueOf(parts[0]), Integer.valueOf(parts[1])));

        }
    }
}


