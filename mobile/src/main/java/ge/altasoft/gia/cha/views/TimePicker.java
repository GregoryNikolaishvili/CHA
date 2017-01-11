package ge.altasoft.gia.cha.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import java.util.Locale;

import ge.altasoft.gia.cha.R;

public class TimePicker extends LinearLayout {

    private View myPickerView;

    private NumberPicker hour_display;
    private NumberPicker min_display;
    private NumberPicker minus_display;

    public TimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context mContext) {
        LayoutInflater inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myPickerView = inflator.inflate(R.layout.time_value_picker, null);
        this.addView(myPickerView);

        initializeReference();
    }

    public class MyTwoDigitFormatter implements NumberPicker.Formatter {
        public String format(int value) {
            return String.format(Locale.US, "%02d", value);
        }
    }

    private void initializeReference() {

        minus_display = (NumberPicker) myPickerView.findViewById(R.id.numberPicker0);

        hour_display = (NumberPicker) myPickerView.findViewById(R.id.numberPicker1);
        min_display = (NumberPicker) myPickerView.findViewById(R.id.numberPicker2);

        minus_display.setMinValue(0);
        minus_display.setMaxValue(1);
        minus_display.setDisplayedValues(new String[]{"+", "-"});

        hour_display.setMinValue(0);
        hour_display.setMaxValue(23);
        hour_display.setFormatter(new MyTwoDigitFormatter());

        min_display.setMinValue(0);
        min_display.setMaxValue(59);
        min_display.setFormatter(new MyTwoDigitFormatter());
    }

    public void setTimeInMinutes(short timeInMinutes) {
        minus_display.setValue(timeInMinutes < 0 ? 1 : 0);
        if (timeInMinutes < 0)
            timeInMinutes = (short) -timeInMinutes;
        hour_display.setValue(timeInMinutes / 60);
        min_display.setValue(timeInMinutes % 60);
    }

    public short getTimeInMinutes() {
        short value = (short) (hour_display.getValue() * 60 + min_display.getValue());
        if (minus_display.getValue() != 0)
            value = (short) -value;
        return value;
    }
}