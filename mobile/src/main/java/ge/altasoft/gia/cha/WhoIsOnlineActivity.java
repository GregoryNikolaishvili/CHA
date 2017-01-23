package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ge.altasoft.gia.cha.classes.CircularArrayList;

public class WhoIsOnlineActivity extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private CircularArrayList<Pair<Integer, String>> logBuffer = null;
    private LogAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_is_active);

        logBuffer = new CircularArrayList<>(10);
        adapter = new LogAdapter(this, logBuffer);

        ListView listView = (ListView) findViewById(R.id.lvWhoIsActive);
        listView.setAdapter(adapter);
    }

    public class LogAdapter extends ArrayAdapter<Pair<Integer, String>> {
        LogAdapter(Context context, ArrayList<Pair<Integer, String>> points) {
            super(context, 0, points);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_key_value, parent, false);
            }

            Pair<Integer, String> point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText("");
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue)).setText(point.second);
            }
            return convertView;
        }
    }

    @Override
    protected void processMqttData(int flags, Intent intent) {
        super.processMqttData(flags, intent);

        if ((flags & Utils.FLAG_HAVE_WHO_IS_ACTIVE) != 0) {
            String value = intent.getStringExtra("value");
            logBuffer.add(new Pair<>(0, value));
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }
}
