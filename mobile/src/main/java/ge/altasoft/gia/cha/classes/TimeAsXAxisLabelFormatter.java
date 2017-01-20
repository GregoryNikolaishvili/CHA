package ge.altasoft.gia.cha.classes;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TimeAsXAxisLabelFormatter extends DefaultLabelFormatter {

    //private final String mFormat;
    final private SimpleDateFormat sdf;

    public TimeAsXAxisLabelFormatter(String format) {
        //mFormat = format;
        sdf = new SimpleDateFormat(format, Locale.US);
    }

    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            // format as date
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) value);
            return sdf.format(calendar.getTimeInMillis());
        } else {
            return super.formatLabel(value, false);
        }
    }
}
