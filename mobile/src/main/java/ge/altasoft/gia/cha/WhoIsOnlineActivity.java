package ge.altasoft.gia.cha;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class WhoIsOnlineActivity extends ChaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_who_is_active);

        ArrayList<String> logBuffer = getIntent().getExtras().getStringArrayList("list");
        LogAdapter adapter = new LogAdapter(this, logBuffer);

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
                ((TextView) convertView.findViewById(R.id.tvListViewItemKey)).setText(point);
                convertView.findViewById(R.id.tvListViewItemValue1).setVisibility(View.GONE);
                convertView.findViewById(R.id.tvListViewItemValue2).setVisibility(View.GONE);
            }
            return convertView;
        }
    }
}
