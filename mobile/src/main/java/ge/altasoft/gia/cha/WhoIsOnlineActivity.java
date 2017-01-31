package ge.altasoft.gia.cha;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class WhoIsOnlineActivity extends ChaActivity {

    final private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.US);
    private ArrayList<String> logBuffer = null;
    private LogAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_is_active);

        logBuffer = getIntent().getExtras().getStringArrayList("list");
        adapter = new LogAdapter(this, logBuffer);

        ListView listView = (ListView) findViewById(R.id.lvWhoIsActive);
        listView.setAdapter(adapter);
    }

    public class LogAdapter extends ArrayAdapter<String> {
        LogAdapter(Context context, ArrayList<String> points) {
            super(context, 0, points);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_key_value, parent, false);
            }

            String point = getItem(position);
            if (point != null) {
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText("");
                ((TextView) convertView.findViewById(R.id.tvListViewItemValue)).setText(point);
            }
            return convertView;
        }
    }

    @Override
    protected void processMqttData(MqttClientLocal.MQTTReceivedDataType dataType, Intent intent) {
        super.processMqttData(dataType, intent);

//        if ((flags & Utils.FLAG_HAVE_WHO_IS_ACTIVE) != 0) {
//            String value = intent.getStringExtra("value");
//            logBuffer.add(new Pair<>(0, value));
//            if (adapter != null)
//                adapter.notifyDataSetChanged();
//        }
    }
}
