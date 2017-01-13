package ge.altasoft.gia.cha;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeAsXAxisLabelFormatter extends DefaultLabelFormatter {

    private final String mFormat;

    public TimeAsXAxisLabelFormatter(String format) {
        mFormat = format;
    }

    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            // format as date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) value);
            SimpleDateFormat dateFormat = new SimpleDateFormat(mFormat, Locale.US);
            return dateFormat.format(calendar.getTimeInMillis());
        } else {
            return super.formatLabel(value, false);
        }
    }
}
